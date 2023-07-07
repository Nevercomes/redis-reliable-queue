package example.rab.business;

import example.rab.framework.RedisTask;
import example.rab.framework.RedisTaskMetadata;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class BusinessService {

    @Resource
    private BusinessTaskScheduler taskExecutor;

    public String createTask() {
        BusinessTask task = BusinessTask.builder()
                .status("prepared")
                .build();
        RedisTaskMetadata metadata = RedisTaskMetadata.builder()
                .taskName("business-task")
                .build();
        RedisTask<BusinessTask> redisTask = RedisTask.<BusinessTask>builder()
                .task(task)
                .metadata(metadata)
                .build();
        taskExecutor.submit(redisTask);

        return redisTask.getTaskId();
    }


}
