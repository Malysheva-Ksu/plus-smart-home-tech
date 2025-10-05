package service;

import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import dto.hub.DeviceAddedEventDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import service.resolver.RawSensorEventHandler;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EventTypeResolver {

    private final List<RawSensorEventHandler> sensorEventHandlers;
    private final ObjectMapper objectMapper;

    public SensorEventDto resolveSensorEvent(Map<String, Object> rawEvent) {
        return sensorEventHandlers.stream()
                .filter(handler -> handler.supports(rawEvent))
                .findFirst()
                .map(handler -> handler.handle(rawEvent))
                .orElseThrow(() -> new IllegalArgumentException("Cannot infer sensor event type: " + rawEvent));
    }

    public HubEventDto resolveHubEvent(Map<String, Object> rawEvent) {
        if (rawEvent.containsKey("type") && rawEvent.containsKey("id")) {
            rawEvent.put("eventType", "DEVICE_ADDED");
            return objectMapper.convertValue(rawEvent, DeviceAddedEventDto.class);
        }
        throw new IllegalArgumentException("Cannot infer hub event type: " + rawEvent);
    }
}