package dev.relay.api;
import dev.relay.domain.*;import dev.relay.repo.WorkflowRepository;import jakarta.validation.Valid;import org.springframework.http.*;import org.springframework.web.bind.annotation.*;import java.util.*;
@RestController @RequestMapping("/api/v1/workflows")
public class WorkflowController {
 private final WorkflowRepository repo;public WorkflowController(WorkflowRepository r){repo=r;}
 @PostMapping public ResponseEntity<Workflow> create(@Valid @RequestBody CreateWorkflowRequest req){Workflow w=repo.create(req);return ResponseEntity.status(HttpStatus.CREATED).body(w);}
 @GetMapping public List<Workflow> all(){return repo.all();}
 @GetMapping("/{id}") public Workflow get(@PathVariable UUID id){return repo.get(id).orElseThrow(()->new NoSuchElementException("Workflow not found"));}
 @GetMapping("/{id}/history") public List<Map<String,Object>> history(@PathVariable UUID id){return repo.history(id);}
 @GetMapping("/{id}/steps") public List<Map<String,Object>> steps(@PathVariable UUID id){return repo.steps(id);}
 @PostMapping("/{id}/cancel") public ResponseEntity<Void> cancel(@PathVariable UUID id){return repo.cancel(id)?ResponseEntity.noContent().build():ResponseEntity.status(409).build();}
 @PostMapping("/{id}/retry") public ResponseEntity<Void> retry(@PathVariable UUID id){return repo.retry(id)?ResponseEntity.accepted().build():ResponseEntity.status(409).build();}
 @ExceptionHandler(NoSuchElementException.class) ResponseEntity<Map<String,String>> missing(Exception e){return ResponseEntity.status(404).body(Map.of("error",e.getMessage()));}
}
