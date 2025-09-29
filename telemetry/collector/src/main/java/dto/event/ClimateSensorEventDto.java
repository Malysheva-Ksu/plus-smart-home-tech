package dto.event;

import dto.abstractDto.SensorEventDto;
import dto.enums.SensorEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class ClimateSensorEventDto extends SensorEventDto {
    @NotNull
    private Integer temperatureC;

    @Min(0)
    @NotNull
    private Integer humidity;

    @Min(0)
    @NotNull
    private Integer co2Level;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.CLIMATE_SENSOR_EVENT;
    }
}