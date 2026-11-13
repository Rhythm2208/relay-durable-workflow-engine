package dev.relay.engine;
import org.springframework.beans.factory.annotation.Value;import org.springframework.context.annotation.Bean;import org.springframework.context.annotation.Configuration;import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;import java.util.concurrent.Executor;
@Configuration
public class WorkerConfiguration {
 @Bean(name="relayWorkerExecutor") Executor relayWorkerExecutor(@Value("${relay.workers:4}") int workers){var e=new ThreadPoolTaskExecutor();e.setCorePoolSize(workers);e.setMaxPoolSize(workers);e.setQueueCapacity(0);e.setThreadNamePrefix("relay-worker-");e.initialize();return e;}
}
