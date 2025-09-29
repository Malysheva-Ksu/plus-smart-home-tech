package dto.hub;

import dto.abstractDto.HubEventDto;
import dto.enums.HubEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioRemovedEventDto extends HubEventDto {
    @NotBlank
    private String name;

    @Override
    public HubEventType getEventType() {
        return HubEventType.SCENARIO_REMOVED_EVENT;
    }
}