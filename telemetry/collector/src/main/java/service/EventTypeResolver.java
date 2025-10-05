package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import dto.hub.DeviceAddedEventDto;
import dto.hub.DeviceRemovedEventDto;
import dto.hub.ScenarioAddedEventDto;
import dto.hub.ScenarioRemovedEventDto;
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
        String eventType = (String) rawEvent.get("type");

        switch (eventType) {
            case "SCENARIO_ADDED":
                return objectMapper.convertValue(rawEvent, ScenarioAddedEventDto.class);
            case "SCENARIO_REMOVED":
                return objectMapper.convertValue(rawEvent, ScenarioRemovedEventDto.class);
            case "DEVICE_ADDED":
                return objectMapper.convertValue(rawEvent, DeviceAddedEventDto.class);
            case "DEVICE_REMOVED":
                return objectMapper.convertValue(rawEvent, DeviceRemovedEventDto.class);
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }
}