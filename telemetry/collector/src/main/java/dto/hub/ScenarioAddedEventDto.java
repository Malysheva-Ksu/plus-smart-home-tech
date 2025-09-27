package dto.hub;

import dto.base.HubEventDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEventDto extends HubEventDto {
    private String scenarioId;
    private String scenarioName;
}