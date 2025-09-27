package service;

import dto.base.HubEventDto;
import dto.base.SensorEventDto;
import dto.hub.ScenarioAddedEventDto;
import dto.hub.ScenarioRemovedEventDto;
import dto.hub.SensorDeregisteredEventDto;
import dto.hub.SensorRegisteredEventDto;
import dto.sensor.*;
import org.springframework.stereotype.Component;

import ru.yandex.practicum.kafka.telemetry.event.*;

@Component
public class TelemetryMapper {

    public SensorData toAvro(SensorEventDto dto) {
        Object event = switch (dto) {
            case LightSensorEventDto e -> mapToLightSensorEvent(e);
            case TemperatureSensorEventDto e -> mapToTemperatureSensorEvent(e);
            case SmartSwitchEventDto e -> mapToSmartSwitchEvent(e);
            case ClimateSensorEventDto e -> mapToClimateSensorEvent(e);
            case MotionSensorEventDto e -> mapToMotionSensorEvent(e);
            default -> throw new IllegalArgumentException("Unsupported SensorEventDto type: " + dto.getClass().getName());
        };
        return new SensorData(event);
    }

    public HubEvent toAvro(HubEventDto dto) {
        Object event = switch (dto) {
            case SensorRegisteredEventDto e -> mapToSensorRegisteredEvent(e);
            case SensorDeregisteredEventDto e -> mapToSensorDeregisteredEvent(e);
            case ScenarioAddedEventDto e -> mapToScenarioAddedEvent(e);
            case ScenarioRemovedEventDto e -> mapToScenarioRemovedEvent(e);
            default -> throw new IllegalArgumentException("Unsupported HubEventDto type: " + dto.getClass().getName());
        };
        return new HubEvent(event);
    }

    private LightSensorEvent mapToLightSensorEvent(LightSensorEventDto dto) {
        return LightSensorEvent.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setLinkQuality(dto.getLinkQuality())
                .setLuminosity(dto.getLuminosity())
                .build();
    }

    private TemperatureSensorEvent mapToTemperatureSensorEvent(TemperatureSensorEventDto dto) {
        return TemperatureSensorEvent.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setLinkQuality(dto.getLinkQuality())
                .setTemperature(dto.getTemperature())
                .build();
    }

    private SmartSwitchEvent mapToSmartSwitchEvent(SmartSwitchEventDto dto) {
        SmartSwitchState avroState = SmartSwitchState.valueOf(dto.getState().name());

        return SmartSwitchEvent.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setLinkQuality(dto.getLinkQuality())
                .setState(avroState)
                .build();
    }

    private ClimateSensorEvent mapToClimateSensorEvent(ClimateSensorEventDto dto) {
        return ClimateSensorEvent.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setLinkQuality(dto.getLinkQuality())
                .setTemperature(dto.getTemperature())
                .setHumidity(dto.getHumidity())
                .build();
    }

    private MotionSensorEvent mapToMotionSensorEvent(MotionSensorEventDto dto) {
        return MotionSensorEvent.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setLinkQuality(dto.getLinkQuality())
                .build();
    }

    private SensorRegisteredEvent mapToSensorRegisteredEvent(SensorRegisteredEventDto dto) {
        SensorType avroSensorType = SensorType.valueOf(dto.getSensorType());

        return SensorRegisteredEvent.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setSensorId(dto.getSensorId())
                .setSensorType(avroSensorType)
                .build();
    }

    private SensorDeregisteredEvent mapToSensorDeregisteredEvent(SensorDeregisteredEventDto dto) {
        return SensorDeregisteredEvent.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setSensorId(dto.getSensorId())
                .build();
    }

    private ScenarioAddedEvent mapToScenarioAddedEvent(ScenarioAddedEventDto dto) {
        return ScenarioAddedEvent.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setScenarioId(dto.getScenarioId())
                .setScenarioName(dto.getScenarioName())
                .build();
    }

    private ScenarioRemovedEvent mapToScenarioRemovedEvent(ScenarioRemovedEventDto dto) {
        return ScenarioRemovedEvent.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setScenarioId(dto.getScenarioId())
                .build();
    }
}