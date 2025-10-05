package service.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.SensorEventDto;
import dto.event.ClimateSensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ClimateSensorEventHandler implements RawSensorEventHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(Map<String, Object> rawEvent) {
        return rawEvent.containsKey("temperature") && rawEvent.containsKey("humidity");
    }

    @Override
    public SensorEventDto handle(Map<String, Object> rawEvent) {
        rawEvent.put("eventType", "CLIMATE_SENSOR");
        return objectMapper.convertValue(rawEvent, ClimateSensorEventDto.class);
    }
}