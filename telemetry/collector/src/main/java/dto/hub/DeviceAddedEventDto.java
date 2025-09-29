package dto.hub;

import dto.base.DeviceTypeDto;
import dto.base.HubEventDto;
import dto.base.HubEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceAddedEventDto extends HubEventDto {
    @NotBlank
    private String id;
    @NotNull
    private DeviceTypeDto type;

    @Override
    public HubEventType getEventType() {
        return HubEventType.DEVICE_ADDED_EVENT;
    }
}