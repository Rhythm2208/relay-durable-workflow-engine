package dev.relay.repo;
import dev.relay.domain.*; import com.fasterxml.jackson.databind.ObjectMapper; import org.springframework.jdbc.core.*; import org.springframework.jdbc.support.*; import org.springframework.stereotype.Repository; import org.springframework.transaction.annotation.Transactional; import org.springframework.dao.DuplicateKeyException;
import java.sql.*; import java.time.*; import java.util.*;
@Repository
public class WorkflowRepository {
 private final JdbcTemplate jdbc; private final ObjectMapper json;
 public WorkflowRepository(JdbcTemplate jdbc,ObjectMapper json){this.jdbc=jdbc;this.json=json;}
 private Workflow map(ResultSet r,int n)throws SQLException{return new Workflow(r.getObject("id",UUID.class),r.getString("workflow_type"),r.getString("idempotency_key"),r.getString("input"),WorkflowStatus.valueOf(r.getString("status")),r.getInt("current_step"),r.getInt("max_attempts"),r.getLong("fencing_token"),r.getTimestamp("created_at").toInstant(),r.getTimestamp("updated_at").toInstant(),r.getTimestamp("completed_at")==null?null:r.getTimestamp("completed_at").toInstant(),r.getString("last_error"));}
 public Optional<Workflow> get(UUID id){return jdbc.query("select * from workflows where id=?",this::map,id).stream().findFirst();}
 public List<Workflow> all(){return jdbc.query("select * from workflows order by created_at desc",this::map);}
 @Transactional
 public Workflow create(CreateWorkflowRequest req){String in;try{in=json.writeValueAsString(req.input());}catch(Exception e){throw new IllegalArgumentException(e);} UUID id=UUID.randomUUID(); int attempts=req.maxAttempts()==null?5:req.maxAttempts();
  int inserted=jdbc.update("insert into workflows(id,workflow_type,idempotency_key,input,status,max_attempts) values (?, ?, ?, ?::jsonb,'PENDING',?) on conflict(idempotency_key) do nothing",id,req.workflowType(),req.idempotencyKey(),in,attempts);
  if(inserted==0)return jdbc.queryForObject("select * from workflows where idempotency_key=?",this::map,req.idempotencyKey());
  try{for(int i=0;i<req.steps().size();i++){var s=req.steps().get(i);jdbc.update("insert into workflow_steps(workflow_id,step_index,activity,config) values(?,?,?,?::jsonb)",id,i,s.activity(),json.writeValueAsString(s.config()==null?Map.of():s.config()));}}catch(Exception e){throw new IllegalStateException(e);} event(id,"CREATED",null,null,null,null,Map.of("type",req.workflowType()));return get(id).orElseThrow();
 }
 public record Claim(UUID id,String workflowType,int step,int attempt,long token,String owner,String activity,String config,String input,int maxAttempts){}
 public Optional<Claim> claim(String owner,int leaseSeconds){return jdbc.query("""
 with candidate as (select id from workflows where ((status in ('PENDING','RETRY_WAIT') and available_at<=now()) or (status='RUNNING' and lease_until<now())) order by available_at,created_at for update skip locked limit 1),
 claimed as (update workflows w set status='RUNNING',lease_owner=?,lease_until=now()+(? * interval '1 second'),fencing_token=fencing_token+1,updated_at=now(),version=version+1 from candidate c where w.id=c.id returning w.*)
 select c.*,s.activity,s.config::text as step_config from claimed c left join workflow_steps s on s.workflow_id=c.id and s.step_index=c.current_step
 """,ps->{ps.setString(1,owner);ps.setInt(2,leaseSeconds);},rs->{if(!rs.next())return Optional.empty();Workflow w=map(rs,1);String activity=rs.getString("activity");if(activity==null)return Optional.of(new Claim(w.id(),w.workflowType(),-1,0,w.fencingToken(),owner,"", "{}",w.input(),w.maxAttempts()));int attempt=jdbc.queryForObject("select count(*)+1 from execution_history where workflow_id=? and step=? and event_type='STEP_FAILED' and id>coalesce((select max(id) from execution_history where workflow_id=? and event_type='MANUAL_RETRY'),0)",Integer.class,w.id(),w.currentStep(),w.id());return Optional.of(new Claim(w.id(),w.workflowType(),w.currentStep(),attempt,w.fencingToken(),owner,activity,rs.getString("step_config"),w.input(),w.maxAttempts()));});}
 public void record(UUID id,String type,int step,int attempt,String worker,long token,Map<String,Object> details){event(id,type,step,attempt,worker,token,details);}
 private void event(UUID id,String type,Integer step,Integer attempt,String worker,Long token,Map<String,Object> data){try{jdbc.update("insert into execution_history(workflow_id,event_type,step,attempt,worker_id,fencing_token,details) values(?,?,?,?,?,?,?::jsonb)",id,type,step,attempt,worker,token,json.writeValueAsString(data));}catch(Exception e){throw new IllegalStateException(e);}}
 @Transactional
 public boolean advance(Claim c, boolean success,String error,boolean terminal,int retrySeconds){String status=success?"PENDING":terminal?"FAILED":"RETRY_WAIT";int changed;
  if(success) changed=jdbc.update("update workflows set current_step=current_step+1,status=case when current_step+1 >= (select count(*) from workflow_steps where workflow_id=?) then 'SUCCEEDED' else 'PENDING' end, completed_at=case when current_step+1 >= (select count(*) from workflow_steps where workflow_id=?) then now() else null end,lease_owner=null,lease_until=null,last_error=null,updated_at=now() where id=? and status='RUNNING' and lease_owner=? and fencing_token=?",c.id(),c.id(),c.id(),c.owner(),c.token());
  else changed=jdbc.update("update workflows set status=?,available_at=now()+(? * interval '1 second'),lease_owner=null,lease_until=null,last_error=?,completed_at=case when ? then now() else null end,updated_at=now() where id=? and status='RUNNING' and lease_owner=? and fencing_token=?",status,retrySeconds,error,terminal,c.id(),c.owner(),c.token());
  if(changed==1){String evt=success?"STEP_SUCCEEDED":terminal?"WORKFLOW_FAILED":"STEP_FAILED";event(c.id(),evt,c.step(),c.attempt(),c.owner(),c.token(),Map.of("error",error==null?"":error));}return changed==1;
 }
 public boolean finishNoSteps(Claim c){return jdbc.update("update workflows set status='SUCCEEDED',completed_at=now(),lease_owner=null,lease_until=null,updated_at=now() where id=? and fencing_token=?",c.id(),c.token())==1;}
 @Transactional
 public boolean cancel(UUID id){int n=jdbc.update("update workflows set status='CANCELLED',completed_at=now(),lease_owner=null,lease_until=null,updated_at=now() where id=? and status in ('PENDING','RETRY_WAIT','RUNNING','FAILED')",id);if(n==1)event(id,"CANCELLED",null,null,null,null,Map.of());return n==1;}
 @Transactional
 public boolean retry(UUID id){int n=jdbc.update("update workflows set status='PENDING',available_at=now(),current_step=0,completed_at=null,last_error=null,updated_at=now() where id=? and status in ('FAILED','CANCELLED')",id);if(n==1)event(id,"MANUAL_RETRY",null,null,null,null,Map.of());return n==1;}
 public List<Map<String,Object>> history(UUID id){return jdbc.queryForList("select id,event_type,step,attempt,worker_id,fencing_token,details,occurred_at from execution_history where workflow_id=? order by id",id);}
 public List<Map<String,Object>> steps(UUID id){return jdbc.queryForList("select step_index,activity,config from workflow_steps where workflow_id=? order by step_index",id);}
}
