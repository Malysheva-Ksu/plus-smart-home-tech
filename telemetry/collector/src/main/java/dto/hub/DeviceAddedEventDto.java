package dto.hub;

import com.fasterxml.jackson.annotation.JsonProperty;
import dto.abstractDto.HubEventDto;
import dto.enums.DeviceTypeDto;
import dto.enums.HubEventType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceAddedEventDto extends HubEventDto {
    @NotBlank
    private String id;

    @NotNull
    @JsonProperty("deviceType")
    private DeviceTypeDto type;

    @Override
    public HubEventType getEventType() {
        return HubEventType.DEVICE_ADDED;
    }
}