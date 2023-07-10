package example.rab.business;

import example.rab.framework.RedisReliableQueue;
import example.rab.framework.RedisReliableQueueFactory;
import example.rab.framework.RedisTaskExecutor;
import example.rab.framework.RedisTaskSchedulerThreadPool;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BusinessTaskConfiguration {

    @Resource
    private RedisReliableQueueFactory queueFactory;

    @Resource
    private RedisTaskExecutor executor;

    @Resource
    private RedisTaskSchedulerThreadPool threadPool;

    @Bean
    public BusinessTaskScheduler businessTaskExecutor() {
        RedisReliableQueue<BusinessTask> queue = queueFactory.create("reliable_queue:task_queue:business",
                "reliable_queue:processing_queue:business", "reliable_queue:task:business:");
        BusinessTaskScheduler scheduler = new BusinessTaskScheduler(queue, executor);
        threadPool.submit(scheduler);
        return scheduler;
    }

}
