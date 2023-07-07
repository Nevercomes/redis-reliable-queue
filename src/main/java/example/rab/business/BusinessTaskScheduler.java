package example.rab.business;

import example.rab.framework.RedisReliableQueue;
import example.rab.framework.RedisTask;
import example.rab.framework.RedisTaskScheduler;
import example.rab.framework.RedisTaskMetadata;
import example.rab.framework.RedisTaskExecutor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BusinessTaskScheduler extends RedisTaskScheduler<BusinessTask> {

    public BusinessTaskScheduler(RedisReliableQueue<BusinessTask> queue, RedisTaskExecutor executor) {
        super(queue, executor);
    }

    @Override
    protected void executeTask(RedisTask<BusinessTask> redisTask) {
        BusinessTask task = redisTask.getTask();
        RedisTaskMetadata metadata = redisTask.getMetadata();
        log.info("task={}, metadata={}", task, metadata);
    }
}
