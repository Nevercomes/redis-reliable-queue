package example.rab.framework;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisReliableQueueFactory {

    @Resource(name = "redisTemplateJdkSerializer")
    private RedisTemplate<String, Object> redisTemplate;

    public <V> RedisReliableQueue<V> create(String taskQueue, String processingQueue, String taskKeyPrefix) {
        return new RedisReliableQueue<>(taskQueue, processingQueue, taskKeyPrefix, redisTemplate);
    }

}
