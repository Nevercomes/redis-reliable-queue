package example.rab.framework;

import java.time.Instant;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;

@Slf4j
public abstract class RedisTaskScheduler<V> implements Runnable {

    protected final RedisReliableQueue<V> queue;

    private final RedisTaskExecutor executor;

    public RedisTaskScheduler(RedisReliableQueue<V> queue, RedisTaskExecutor executor) {
        this.queue = queue;
        this.executor = executor;
    }

    protected abstract void executeTask(RedisTask<V> task);

    @SuppressWarnings({"InfiniteLoopStatement"})
    @Override
    public void run() {
        log.info("Start to listen redis task...");
        long checkInterval = 60000;
        long lastCheckTime = Instant.now().toEpochMilli();
        while (true) {
            long currentTime = Instant.now().toEpochMilli();
            if (currentTime - lastCheckTime >= checkInterval) {
                checkProcessingTasks();
                lastCheckTime = currentTime;
            }

            RedisTask<V> task = queue.blockGet();
            if (ObjectUtils.isNotEmpty(task)) {
                try {
                    executor.submit(() -> handleTask(task));
                } catch (RejectedExecutionException e) {
                    log.error("task={} has been rejected, message={}", task.getMetadata(), e.getMessage());
                    queue.nack(task);
                }
            }
        }
    }

    private void handleTask(RedisTask<V> task) {
        try {
            executeTask(task);
            queue.ack(task);
        } catch (Exception e) {
            log.error("task={} execute occurs exception: {}", task.getMetadata(), e.getMessage());
            queue.nack(task);
        }
    }

    private void checkProcessingTasks() {
        // Check if there are any tasks that have been processing for too long
        // If so, nack them back to the task queue for reprocessing
        try {
            List<RedisTask<V>> processingTasks = queue.getAllProcessingTasks();
            for (RedisTask<V> task : processingTasks) {
                if (task.getMetadata().isTaskProcessTimeout()) {
                    queue.nack(task);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void submit(RedisTask<V> task) {
        queue.push(task);
    }


    public void saveTask(RedisTask<V> task) {
        queue.saveTask(task);
    }

    public RedisTask<V> getTask(String taskId) {
        return queue.getTask(taskId);
    }
}
