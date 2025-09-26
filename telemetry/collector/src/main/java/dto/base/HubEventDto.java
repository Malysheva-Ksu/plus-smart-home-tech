package dto.base;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dto.hub.ScenarioAddedEventDto;
import dto.hub.ScenarioRemovedEventDto;
import dto.hub.SensorDeregisteredEventDto;
import dto.hub.SensorRegisteredEventDto;
import lombok.Data;
import java.time.Instant;

@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SensorRegisteredEventDto.class, name = "SENSOR_REGISTERED_EVENT"),
        @JsonSubTypes.Type(value = SensorDeregisteredEventDto.class, name = "SENSOR_DEREGISTERED_EVENT"),
        @JsonSubTypes.Type(value = ScenarioAddedEventDto.class, name = "SCENARIO_ADDED_EVENT"),
        @JsonSubTypes.Type(value = ScenarioRemovedEventDto.class, name = "SCENARIO_REMOVED_EVENT")
})
public abstract class HubEventDto {
    private String hubId;
    private Instant timestamp;
}