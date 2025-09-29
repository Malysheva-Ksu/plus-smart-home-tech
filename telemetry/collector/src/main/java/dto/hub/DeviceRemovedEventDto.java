package dto.hub;

import dto.abstractDto.HubEventDto;
import dto.enums.HubEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceRemovedEventDto extends HubEventDto {
    @NotBlank
    private String id;

    @Override
    public HubEventType getEventType() {
        return HubEventType.DEVICE_REMOVED;
    }
}