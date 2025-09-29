package dto.event;

import dto.abstractDto.SensorEventDto;
import dto.enums.SensorEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UnknownSensorEventDto extends SensorEventDto {
    @Override
    public SensorEventType getEventType() {
        return SensorEventType.UNKNOWN_SENSOR;
    }
}