package dto.base;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UnknownSensorEventDto extends SensorEventDto {
    @Override
    public SensorEventType getEventType() {
        return SensorEventType.UNKNOWN_SENSOR_EVENT;
    }
}