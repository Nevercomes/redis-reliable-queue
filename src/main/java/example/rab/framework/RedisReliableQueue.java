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

    private final Integer blockTimeout;

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisReliableQueue(String taskQueue, String processingQueue, RedisTemplate<String, Object> redisTemplate) {
        this.taskQueue = taskQueue;
        this.processingQueue = processingQueue;
        this.redisTemplate = redisTemplate;
        this.blockTimeout = 10;
        log.info("Init redis reliable queue with task_queue={}, processing_queue={}, block_timeout={} finished",
                taskQueue, processingQueue, blockTimeout);
    }

    public void push(RedisTask<V> task) {
        saveTask(task);
        redisTemplate.opsForList().leftPush(taskQueue, task.getTaskId());
    }

    @SuppressWarnings({"unchecked"})
    public RedisTask<V> blockGet() {
        Object taskIdObj = redisTemplate.opsForList().rightPopAndLeftPush(taskQueue, processingQueue, blockTimeout, TimeUnit.SECONDS);
        if (ObjectUtils.isNotEmpty(taskIdObj)) {
            log.info("read task={}", taskIdObj);
            String taskId = String.valueOf(taskIdObj);
            try {
                RedisTask<V> task = (RedisTask<V>) redisTemplate.opsForValue().get(taskId);
                if (ObjectUtils.isNotEmpty(task)) {
                    // update task processTime when it is taken, but is may not necessary
                    // because in most situation, the processTime is almost equal to the createTime
                    // so the update code has been removed
                    return task;
                } else {
                    // Task has expired, remove the taskId from processingQueue
                    redisTemplate.opsForList().remove(processingQueue, 1, taskId);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        log.info("task read timeout");
        return null;
    }

    public void ack(RedisTask<V> task) {
        String taskId = task.getTaskId();
        redisTemplate.expire(taskId, task.getMetadata().getFinishedTaskExpireTime(), TimeUnit.SECONDS);
        redisTemplate.opsForList().remove(processingQueue, 1, taskId);
    }

    public void nack(RedisTask<V> task) {
        // TODO transaction and concurrent control
        String taskId = task.getTaskId();
        Long removed = redisTemplate.opsForList().remove(processingQueue, 1, taskId);
        if (ObjectUtils.isNotEmpty(removed) && removed > 0) {
            redisTemplate.expire(taskId, task.getMetadata().getTaskExpireTime(), TimeUnit.SECONDS);
            redisTemplate.opsForList().leftPush(taskQueue, taskId);
        }
    }

    public List<String> getAllProcessingTaskIds() {
        return Optional.ofNullable(redisTemplate.opsForList().range(processingQueue, 0, -1))
                .orElse(Collections.emptyList())
                .stream()
                .map(Object::toString)
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

    public void saveTask(RedisTask<V> task) {
        redisTemplate.opsForValue().set(task.getTaskId(), task, task.getMetadata().getTaskExpireTime(), TimeUnit.SECONDS);
    }

    @SuppressWarnings({"unchecked"})
    public RedisTask<V> getTask(String taskId) {
        return (RedisTask<V>) redisTemplate.opsForValue().get(taskId);
    }
}
