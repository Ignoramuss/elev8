package io.elev8.resources.cronjob;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.elev8.resources.ObjectReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CronJobStatus {
    private List<ObjectReference> active;
    private String lastScheduleTime;
    private String lastSuccessfulTime;
}
