package dto.event;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import dto.abstractDto.SensorEventDto;
import dto.enums.SensorEventType;
import lombok.Getter;
import java.util.HashMap;
import java.util.Map;

@Getter
public class UnknownSensorEventDto extends SensorEventDto {

    private final Map<String, Object> unknownFields = new HashMap<>();

    @Override
    public SensorEventType getEventType() {
        return SensorEventType.UNKNOWN_SENSOR;
    }

    @JsonAnySetter
    public void setUnknownField(String name, Object value) {
        unknownFields.put(name, value);
    }
}