package example.rab.framework;

import jakarta.annotation.Resource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class RedisReliableQueueFactory {

    @Resource
    private RedisTemplate<String, Object> redisTemplateWithTx;

    public <V> RedisReliableQueue<V> create(String taskQueue, String processingQueue) {
        return new RedisReliableQueue<>(taskQueue, processingQueue, redisTemplateWithTx);
    }

}
