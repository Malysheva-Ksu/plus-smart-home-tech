package dto.sensor;

import dto.base.SensorEventDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEventDto extends SensorEventDto {
    private int linkQuality;
    private float temperature;
    private float humidity;
}