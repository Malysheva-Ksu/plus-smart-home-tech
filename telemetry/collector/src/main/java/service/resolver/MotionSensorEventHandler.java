package service.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.SensorEventDto;
import dto.event.MotionSensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class MotionSensorEventHandler implements RawSensorEventHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(Map<String, Object> rawEvent) {
        return rawEvent.containsKey("motion");
    }

    @Override
    public SensorEventDto handle(Map<String, Object> rawEvent) {
        rawEvent.put("eventType", "MOTION_SENSOR");
        return objectMapper.convertValue(rawEvent, MotionSensorEventDto.class);
    }
}