package dto.sensor;

import dto.base.SensorEventDto;
import dto.base.SensorEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class SwitchSensorEventDto extends SensorEventDto {
    @NotNull
    private Boolean state;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.SWITCH_SENSOR_EVENT;
    }
}