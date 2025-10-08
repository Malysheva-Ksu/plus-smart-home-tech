package dto.hub;

import dto.abstractDto.HubEventDto;
import dto.enums.HubEventType;
import dto.nested.DeviceActionDto;
import dto.nested.ScenarioConditionDto;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.Valid;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScenarioAddedEventDto extends HubEventDto {
    @NotBlank
    private String name;

    @NotEmpty
    @Valid
    private List<ScenarioConditionDto> conditions;

    @NotEmpty
    @Valid
    private List<DeviceActionDto> actions;

    @Override
    public HubEventType getEventType() {
        return HubEventType.SCENARIO_ADDED;
    }
}