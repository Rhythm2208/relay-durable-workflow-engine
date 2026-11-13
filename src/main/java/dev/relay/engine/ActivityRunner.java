package dev.relay.engine;
import com.fasterxml.jackson.databind.*;import org.springframework.stereotype.Component;import java.util.UUID;
@Component public class ActivityRunner {
 private final ObjectMapper mapper;private final OrderActivityService orders;
 public ActivityRunner(ObjectMapper mapper,OrderActivityService orders){this.mapper=mapper;this.orders=orders;}
 public void run(UUID workflowId,String activity,String config,String input,int attempt)throws Exception {
  JsonNode c=mapper.readTree(config);int delay=c.path("delayMs").asInt(0);if(delay>0)Thread.sleep(Math.min(delay,30000));
  switch(activity){case "noop","log","echo" -> {} case "fail" -> {int times=c.path("times").asInt(Integer.MAX_VALUE);if(attempt<=times)throw new SimulatedFailure("simulated failure (times="+times+")");} case "fail-once" -> {if(attempt==1)throw new SimulatedFailure("simulated one-shot failure");} case "random-failure" -> {if(Math.random()<c.path("probability").asDouble(.5))throw new SimulatedFailure("simulated random failure");}
   case "reserve-inventory","charge-payment","create-shipment","send-confirmation" -> {JsonNode o=mapper.readTree(input);switch(activity){case "reserve-inventory"->orders.reserve(workflowId,o);case "charge-payment"->orders.charge(workflowId,o,attempt);case "create-shipment"->orders.ship(workflowId,o);case "send-confirmation"->orders.notifyCustomer(workflowId,o);}}
   default -> throw new IllegalArgumentException("Unknown activity: "+activity);}
 }
 public boolean compensate(UUID workflowId)throws Exception{return orders.compensate(workflowId);}
 public static class SimulatedFailure extends Exception {public SimulatedFailure(String m){super(m);}}
}
