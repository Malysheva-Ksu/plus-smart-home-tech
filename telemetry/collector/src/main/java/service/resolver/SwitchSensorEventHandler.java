package service.resolver;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.SensorEventDto;
import dto.event.SwitchSensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SwitchSensorEventHandler implements RawSensorEventHandler {

    private final ObjectMapper objectMapper;

    @Override
    public boolean supports(Map<String, Object> rawEvent) {
        return rawEvent.containsKey("state");
    }

    @Override
    public SensorEventDto handle(Map<String, Object> rawEvent) {
        rawEvent.put("eventType", "SWITCH_SENSOR");
        return objectMapper.convertValue(rawEvent, SwitchSensorEventDto.class);
    }
}