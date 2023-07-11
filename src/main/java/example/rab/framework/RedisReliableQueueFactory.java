package example.rab.framework;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisReliableQueueFactory {

    private static final String TASK_QUEUE_PREFIX = "reliable_queue:task_queue:";

    private static final String PROCESSING_QUEUE_PREFIX = "reliable_queue:processing_queue:";

    private static final String TASK_METADATA_KEY_PREFIX = "reliable_queue:task_metadata:";

    @Resource(name = "redisTemplateJdkSerializer")
    private RedisTemplate<String, Object> redisTemplate;

    public <V> RedisReliableQueue<V> create(String taskName) {
        String taskQueue = TASK_QUEUE_PREFIX + taskName;
        String processingQueue = PROCESSING_QUEUE_PREFIX + taskQueue;
        String taskKeyPrefix = TASK_METADATA_KEY_PREFIX + taskName + ":";
        return new RedisReliableQueue<>(taskQueue, processingQueue, taskKeyPrefix, redisTemplate);
    }

}
