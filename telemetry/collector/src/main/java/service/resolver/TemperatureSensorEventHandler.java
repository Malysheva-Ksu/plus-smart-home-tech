package service.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.event.TemperatureSensorEventDto;
import dto.abstractDto.SensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class TemperatureSensorEventHandler implements RawSensorEventHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(Map<String, Object> rawEvent) {
        return rawEvent.containsKey("temperatureC") && !rawEvent.containsKey("humidity");
    }

    @Override
    public SensorEventDto handle(Map<String, Object> rawEvent) {
        rawEvent.put("eventType", "TEMPERATURE_SENSOR");
        return objectMapper.convertValue(rawEvent, TemperatureSensorEventDto.class);
    }
}