package service;

import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;

import java.util.Map;

public interface TelemetryService {
    void save(SensorEventDto event);

    void save(HubEventDto event);

    void processRawSensorEvent(Map<String, Object> rawEvent);

    void processRawHubEvent(Map<String, Object> rawEvent);
}