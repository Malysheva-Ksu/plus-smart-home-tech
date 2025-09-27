package dto.sensor;

import dto.base.SensorEventDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotionSensorEventDto extends SensorEventDto {
    private int linkQuality;
}