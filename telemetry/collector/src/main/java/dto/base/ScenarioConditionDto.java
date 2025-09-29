package dto.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScenarioConditionDto {
    private String sensorId;
    private ConditionTypeDto type;
    private ConditionOperationDto operation;
    private Object value;
}