package example.rab.framework;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

@Component
public class RedisTaskSchedulerThreadPool {

    private final AtomicInteger threadNumber = new AtomicInteger(1);

    private final ExecutorService executor;

    public RedisTaskSchedulerThreadPool() {
        executor = new ThreadPoolExecutor(0,
                10,
                60, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10), r -> {
            Thread thread = new Thread(r);
            thread.setName("task-scheduler-" + threadNumber.getAndIncrement());
            return thread;
        });
    }

    public void submit(Runnable task) {
        executor.submit(task);
    }

}
