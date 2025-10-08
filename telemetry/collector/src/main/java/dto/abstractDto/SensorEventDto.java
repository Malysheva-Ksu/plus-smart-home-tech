package dto.abstractDto;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import dto.enums.SensorEventType;
import dto.event.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;

@Data
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "eventType",
        defaultImpl = UnknownSensorEventDto.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = LightSensorEventDto.class, name = "LIGHT_SENSOR"),
        @JsonSubTypes.Type(value = TemperatureSensorEventDto.class, name = "TEMPERATURE_SENSOR"),
        @JsonSubTypes.Type(value = SwitchSensorEventDto.class, name = "SWITCH_SENSOR"),
        @JsonSubTypes.Type(value = ClimateSensorEventDto.class, name = "CLIMATE_SENSOR"),
        @JsonSubTypes.Type(value = MotionSensorEventDto.class, name = "MOTION_SENSOR")
})
public abstract class SensorEventDto {
    @NotBlank
    private String id;
    @NotBlank
    private String hubId;
    @NotNull
    private Instant timestamp;

    @NotNull
    public abstract SensorEventType getEventType();
}