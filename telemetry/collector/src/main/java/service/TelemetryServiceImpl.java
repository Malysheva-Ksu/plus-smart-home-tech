package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import dto.event.*;
import dto.hub.DeviceAddedEventDto;
import kafka.TelemetryEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final TelemetryEventProducer eventProducer;
    private final TelemetryMapper telemetryMapper;
    private final ObjectMapper objectMapper;

    @Override
    public void save(SensorEventDto event) {
        var avro = telemetryMapper.toAvro(event);
        eventProducer.send(avro);
        log.info("Sensor event saved: {}", event);
    }

    @Override
    public void save(HubEventDto event) {
        var avro = telemetryMapper.toAvro(event);
        eventProducer.send(avro);
        log.info("Hub event saved: {}", event);
    }

    @Override
    public void processRawSensorEvent(Map<String, Object> rawEvent) {
        log.info("Processing raw sensor event: {}", rawEvent);
        SensorEventDto eventDto = inferSensorEventDto(rawEvent);
        save(eventDto);
    }

    @Override
    public void processRawHubEvent(Map<String, Object> rawEvent) {
        log.info("Processing raw hub event: {}", rawEvent);
        HubEventDto eventDto = inferHubEventDto(rawEvent);
        save(eventDto);
    }

    private SensorEventDto inferSensorEventDto(Map<String, Object> rawEvent) {
        if (rawEvent.containsKey("temperature") && rawEvent.containsKey("humidity")) {
            rawEvent.put("eventType", "CLIMATE_SENSOR");
            return objectMapper.convertValue(rawEvent, ClimateSensorEventDto.class);
        }
        if (rawEvent.containsKey("temperatureC")) {
            rawEvent.put("eventType", "TEMPERATURE_SENSOR");
            return objectMapper.convertValue(rawEvent, TemperatureSensorEventDto.class);
        }
        if (rawEvent.containsKey("luminosity")) {
            rawEvent.put("eventType", "LIGHT_SENSOR");
            return objectMapper.convertValue(rawEvent, LightSensorEventDto.class);
        }
        if (rawEvent.containsKey("motion")) {
            rawEvent.put("eventType", "MOTION_SENSOR");
            return objectMapper.convertValue(rawEvent, MotionSensorEventDto.class);
        }
        if (rawEvent.containsKey("state")) {
            rawEvent.put("eventType", "SWITCH_SENSOR");
            return objectMapper.convertValue(rawEvent, SwitchSensorEventDto.class);
        }
        throw new IllegalArgumentException("Cannot infer sensor event type" + rawEvent);
    }


    private HubEventDto inferHubEventDto(Map<String, Object> rawEvent) {
        if (rawEvent.containsKey("type") && rawEvent.containsKey("id")) {
            rawEvent.put("eventType", "DEVICE_ADDED");
            return objectMapper.convertValue(rawEvent, DeviceAddedEventDto.class);
        }
        throw new IllegalArgumentException("Cannot infer hub event type" + rawEvent);
    }
}