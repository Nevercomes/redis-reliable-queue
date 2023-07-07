package example.rab.framework;

import jakarta.annotation.PostConstruct;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class RedisTaskExecutor {

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    @Value("${application.thread-pool.redis-task.core-size}")
    private Integer coreSize = 3;
    @Value("${application.thread-pool.redis-task.max-size}")
    private Integer maxSize = 20;
    @Value("${application.thread-pool.redis-task.idle-time}")
    private Integer idleTime = 60;
    @Value("${application.thread-pool.redis-task.queue-capacity}")
    private Integer queueCapacity = 100;
    private ExecutorService executor;

    // use post construct to init executor since the @Value is not loaded while its bean is constructing
    @PostConstruct
    public void initExecutor() {
        executor = new ThreadPoolExecutor(coreSize,
                maxSize,
                idleTime, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(queueCapacity), r -> {
            Thread thread = new Thread(r);
            thread.setName("redis-task-" + threadNumber.getAndIncrement());
            return thread;
        });
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }
}
