package dto.abstractDto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dto.enums.HubEventType;
import dto.hub.UnknownHubEventDto;
import dto.hub.DeviceAddedEventDto;
import dto.hub.DeviceRemovedEventDto;
import dto.hub.ScenarioAddedEventDto;
import dto.hub.ScenarioRemovedEventDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        defaultImpl = UnknownHubEventDto.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = DeviceAddedEventDto.class, name = "DEVICE_ADDED"),
        @JsonSubTypes.Type(value = DeviceRemovedEventDto.class, name = "DEVICE_REMOVED"),
        @JsonSubTypes.Type(value = ScenarioAddedEventDto.class, name = "SCENARIO_ADDED"),
        @JsonSubTypes.Type(value = ScenarioRemovedEventDto.class, name = "SCENARIO_REMOVED")
})
public abstract class HubEventDto {
    @NotBlank
    private String hubId;
    @NotNull
    private Instant timestamp;

    @NotNull
    public abstract HubEventType getEventType();
}