package example.rab.framework;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RedisTaskMetadata implements Serializable {

    @Builder.Default
    private String taskId = String.valueOf(UUID.randomUUID());

    @Builder.Default
    private String taskName = "redis-task";

    @Builder.Default
    // 1hour
    private Integer taskExpireTime = 3600;

    @Builder.Default
    // 30s
    private Integer finishedTaskExpireTime = 30;

    @Builder.Default
    // 7 days
    private Integer failedTaskExpireTime = 7 * 24 *3600;

    @Builder.Default
    private Integer retryCount = 0;

    @Builder.Default
    private Integer retryMax = 3;

    @Builder.Default
    private Integer processTimeout = 180;

    // the time of the task moved from task_queue to processing_queue time
    private Long processTime;

    @Builder.Default
    private Long createTime = Instant.now().toEpochMilli();

    public boolean isTaskProcessTimeout() {
        Long time = ObjectUtils.isNotEmpty(processTime) ? processTime : createTime;
        return time + processTimeout * 1000 <= Instant.now().toEpochMilli();
    }

}
