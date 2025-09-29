package dto.event;

import dto.abstractDto.SensorEventDto;
import dto.enums.SensorEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class MotionSensorEventDto extends SensorEventDto {
    @Min(0)
    private int linkQuality;

    @NotNull
    private Boolean motion;

    @Min(0)
    private Integer voltage;

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.MOTION_SENSOR_EVENT;
    }
}