package example.rab.framework;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(force = true)
public class RedisTask<V> {

    private V task;

    @NonNull
    private RedisTaskMetadata metadata;

    public String getTaskId() {
        return metadata.getTaskId();
    }

}
