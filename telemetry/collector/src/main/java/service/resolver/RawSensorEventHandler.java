package service.resolver;

import dto.abstractDto.SensorEventDto;

import java.util.Map;

public interface RawSensorEventHandler {

    boolean supports(Map<String, Object> rawEvent);

    SensorEventDto handle(Map<String, Object> rawEvent);
}