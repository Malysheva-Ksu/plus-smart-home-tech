package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import dto.enums.DeviceTypeDto;
import dto.hub.DeviceAddedEventDto;
import dto.hub.DeviceRemovedEventDto;
import dto.hub.ScenarioAddedEventDto;
import dto.hub.ScenarioRemovedEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import service.resolver.RawSensorEventHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
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
        Map<String, Object> modifiedEvent = new HashMap<>(rawEvent);

        modifiedEvent.put("eventType", modifiedEvent.get("type"));

        String eventType = (String) modifiedEvent.get("eventType");

        if ("DEVICE_ADDED".equals(eventType)) {
            Object deviceTypeValue = modifiedEvent.get("deviceType");
            if (deviceTypeValue instanceof String) {
                modifiedEvent.put("type", DeviceTypeDto.valueOf((String) deviceTypeValue));
            }

            return objectMapper.convertValue(modifiedEvent, DeviceAddedEventDto.class);
        }

        switch (eventType) {
            case "SCENARIO_ADDED":
                return objectMapper.convertValue(modifiedEvent, ScenarioAddedEventDto.class);
            case "SCENARIO_REMOVED":
                return objectMapper.convertValue(modifiedEvent, ScenarioRemovedEventDto.class);
            case "DEVICE_REMOVED":
                return objectMapper.convertValue(modifiedEvent, DeviceRemovedEventDto.class);
            default:
                throw new IllegalArgumentException("Unsupported event type: " + eventType);
        }
    }

    private String extractEventType(Map<String, Object> rawEvent) {
        Object eventType = rawEvent.get("eventType");

        if (eventType == null) {
            eventType = rawEvent.get("type");
        }

        if (eventType == null) {
            log.warn("Event type not found in raw event: {}", rawEvent);
            return "UNKNOWN";
        }

        return eventType.toString();
    }
}