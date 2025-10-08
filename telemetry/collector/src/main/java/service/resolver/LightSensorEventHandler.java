package service.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.SensorEventDto;
import dto.event.LightSensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class LightSensorEventHandler implements RawSensorEventHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(Map<String, Object> rawEvent) {
        return rawEvent.containsKey("luminosity");
    }

    @Override
    public SensorEventDto handle(Map<String, Object> rawEvent) {
        rawEvent.put("eventType", "LIGHT_SENSOR");
        return objectMapper.convertValue(rawEvent, LightSensorEventDto.class);
    }
}