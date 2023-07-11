package example.rab.framework;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.RedisTemplate;

@Slf4j
public class RedisReliableQueue<V> {

    private final String taskQueue;

    private final String processingQueue;

    private final String taskMetadataPrefix;

    private final Integer blockTimeout;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisReliableQueue(String taskQueue, String processingQueue, String taskMetadataPrefix,
                              RedisTemplate<String, Object> redisTemplate) {
        this.taskQueue = taskQueue;
        this.processingQueue = processingQueue;
        this.taskMetadataPrefix = taskMetadataPrefix;
        this.redisTemplate = redisTemplate;
        this.blockTimeout = 10;
        log.info("Init redis reliable queue with task_queue={}, processing_queue={}, block_timeout={} finished",
                taskQueue, processingQueue, blockTimeout);
    }

    public void submit(RedisTask<V> task) {
        saveTaskInfo(task);
        redisTemplate.opsForList().leftPush(taskQueue, task.getTaskId());
    }

    @SuppressWarnings({"unchecked"})
    public RedisTask<V> blockGet() {
        Object taskIdObj =
                redisTemplate.opsForList().rightPopAndLeftPush(taskQueue, processingQueue, blockTimeout, TimeUnit.SECONDS);
        if (ObjectUtils.isNotEmpty(taskIdObj)) {
            String taskId = String.valueOf(taskIdObj);
            String metadataKey = constructTaskMetadataKey(taskId);
            log.info("read task={} from task queue", metadataKey);
            try {
                RedisTask<V> task = (RedisTask<V>) redisTemplate.opsForValue().get(metadataKey);
                if (ObjectUtils.isNotEmpty(task)) {
                    // update task processTime when it is taken, but is may not necessary
                    // because in most situation, the processTime is almost equal to the createTime
                    // so the update code has been removed
                    return task;
                } else {
                    // Task has expired, remove the taskId from processingQueue
                    log.warn("task={} read from task_queue, but metadata not found, maybe expired", metadataKey);
                    redisTemplate.opsForList().remove(processingQueue, 1, taskId);
                }
            } catch (Exception e) {
                log.error("occurs error while block get, exception:{}", e.getMessage());
            }
        }
        return null;
    }

    public void ack(RedisTask<V> task) {
        String taskId = task.getTaskId();
        String metadataKey = constructTaskMetadataKey(task.getTaskId());
        redisTemplate.expire(metadataKey, task.getMetadata().getFinishedTaskExpireTime(), TimeUnit.SECONDS);
        redisTemplate.opsForList().remove(processingQueue, 1, taskId);
    }

    public void nack(RedisTask<V> task) {
        String taskId = task.getTaskId();
        String metadataKey = constructTaskMetadataKey(taskId);
        log.info("nack task={}", metadataKey);
        Long removed = redisTemplate.opsForList().remove(processingQueue, 1, taskId);
        if (ObjectUtils.isNotEmpty(removed) && removed > 0) {
            log.info("removed task={} from processing queue", metadataKey);
            int retryCount = task.getMetadata().getRetryCount() + 1;
            if (retryCount > task.getMetadata().getRetryMax()) {
                log.error("task={} has reached retry max, it will be discarded, "
                        + "but its metadata will be save in cache", task.getMetadata());
                redisTemplate.expire(metadataKey, task.getMetadata().getFailedTaskExpireTime(), TimeUnit.SECONDS);
                return;
            }
            task.getMetadata().setRetryCount(retryCount);
            saveTaskInfo(task);
            redisTemplate.opsForList().leftPush(taskQueue, taskId);
        }
    }

    public List<String> getAllProcessingTaskIds() {
        return Optional.ofNullable(redisTemplate.opsForList().range(processingQueue, 0, -1))
                .orElse(Collections.emptyList())
                .stream()
                .map(id -> constructTaskMetadataKey(String.valueOf(id)))
                .collect(Collectors.toList());
    }

    @SuppressWarnings({"unchecked"})
    public List<RedisTask<V>> getAllProcessingTasks() {
        return Optional.ofNullable(redisTemplate.opsForValue().multiGet(getAllProcessingTaskIds()))
                .orElse(Collections.emptyList())
                .stream()
                .map(task -> (RedisTask<V>) task)
                .collect(Collectors.toList());
    }

    public void saveTaskInfo(RedisTask<V> task) {
        String metadataKey = constructTaskMetadataKey(task.getTaskId());
        redisTemplate.opsForValue().set(metadataKey, task, task.getMetadata().getTaskExpireTime(),
                TimeUnit.SECONDS);
    }

    @SuppressWarnings({"unchecked"})
    public RedisTask<V> getTaskInfo(String taskId) {
        String metadataKey = constructTaskMetadataKey(taskId);
        return (RedisTask<V>) redisTemplate.opsForValue().get(metadataKey);
    }

    private String constructTaskMetadataKey(String taskId) {
        return taskMetadataPrefix + taskId;
    }
}
