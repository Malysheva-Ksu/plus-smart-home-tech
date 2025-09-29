package dto.sensor;

import dto.base.SensorEventDto;
import dto.base.SensorEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Min;

@Data
@EqualsAndHashCode(callSuper = true)
public class LightSensorEventDto extends SensorEventDto {
    @Min(0)
    private int linkQuality;

    @Min(0)
    private int luminosity;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.LIGHT_SENSOR_EVENT;
    }
}