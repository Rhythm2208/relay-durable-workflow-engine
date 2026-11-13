package dev.relay.engine;
import com.fasterxml.jackson.databind.ObjectMapper;import org.junit.jupiter.api.Test;import java.util.UUID;import static org.junit.jupiter.api.Assertions.*;
class ActivityRunnerTest {
 private ActivityRunner runner(){return new ActivityRunner(new ObjectMapper(),new OrderActivityService(null));}
 @Test void failOnceSucceedsOnRetry()throws Exception{var a=runner();assertThrows(ActivityRunner.SimulatedFailure.class,()->a.run(UUID.randomUUID(),"fail-once","{}","{}",1));assertDoesNotThrow(()->a.run(UUID.randomUUID(),"fail-once","{}","{}",2));}
 @Test void unknownActivityRejected(){assertThrows(IllegalArgumentException.class,()->runner().run(UUID.randomUUID(),"bad","{}","{}",1));}
}
