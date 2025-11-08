package service;

import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import dto.nested.*;
import dto.event.*;
import dto.hub.*;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TelemetryMapper {

    public SensorEventAvro toAvro(SensorEventDto dto) {
        Object payload = switch (dto) {
            case LightSensorEventDto e -> mapToLightSensorAvro(e);
            case TemperatureSensorEventDto e -> mapToTemperatureSensorAvro(e);
            case SwitchSensorEventDto e -> mapToSwitchSensorAvro(e);
            case ClimateSensorEventDto e -> mapToClimateSensorAvro(e);
            case MotionSensorEventDto e -> mapToMotionSensorAvro(e);
            case UnknownSensorEventDto e -> {
                Object unknownEventType = e.getUnknownFields().get("eventType");
                String errorMessage = (unknownEventType != null)
                        ? "Received unknown eventType: '" + unknownEventType + "'"
                        : "Field 'eventType' was missing in the request.";
                throw new IllegalArgumentException("Unsupported SensorEventDto. " + errorMessage);
            }
            default ->
                    throw new IllegalArgumentException("Unsupported SensorEventDto type: " + dto.getClass().getName());
        };

        return SensorEventAvro.newBuilder()
                .setId(dto.getId())
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setPayload(payload)
                .build();
    }

    public HubEventAvro toAvro(HubEventDto dto) {
        Object payload = switch (dto) {
            case DeviceAddedEventDto e -> mapToDeviceAddedEventAvro(e);
            case DeviceRemovedEventDto e -> mapToDeviceRemovedEventAvro(e);
            case ScenarioAddedEventDto e -> mapToScenarioAddedEventAvro(e);
            case ScenarioRemovedEventDto e -> mapToScenarioRemovedEventAvro(e);
            case UnknownHubEventDto e -> {
                Object unknownEventType = e.getUnknownFields().get("eventType");
                String errorMessage = (unknownEventType != null)
                        ? "Received unknown eventType: '" + unknownEventType + "'"
                        : "Field 'eventType' was missing in the request.";
                throw new IllegalArgumentException("Unsupported HubEventDto. " + errorMessage);
            }
            default -> throw new IllegalArgumentException("Unsupported HubEventDto type: " + dto.getClass().getName());
        };

        return HubEventAvro.newBuilder()
                .setHubId(dto.getHubId())
                .setTimestamp(dto.getTimestamp())
                .setPayload(payload)
                .build();
    }

    private LightSensorAvro mapToLightSensorAvro(LightSensorEventDto dto) {
        return LightSensorAvro.newBuilder()
                .setLinkQuality(dto.getLinkQuality())
                .setLuminosity(dto.getLuminosity())
                .build();
    }

    private TemperatureSensorAvro mapToTemperatureSensorAvro(TemperatureSensorEventDto dto) {
        return TemperatureSensorAvro.newBuilder()
                .setTemperatureC(dto.getTemperatureC())
                .setTemperatureF((int) (dto.getTemperatureC() * 1.8 + 32))
                .build();
    }

    private SwitchSensorAvro mapToSwitchSensorAvro(SwitchSensorEventDto dto) {
        return SwitchSensorAvro.newBuilder()
                .setState(dto.getState())
                .build();
    }

    private ClimateSensorAvro mapToClimateSensorAvro(ClimateSensorEventDto dto) {
        return ClimateSensorAvro.newBuilder()
                .setTemperatureC(dto.getTemperatureC())
                .setHumidity(dto.getHumidity())
                .setCo2Level(dto.getCo2Level())
                .build();
    }

    private MotionSensorAvro mapToMotionSensorAvro(MotionSensorEventDto dto) {
        return MotionSensorAvro.newBuilder()
                .setLinkQuality(dto.getLinkQuality())
                .setMotion(dto.getMotion())
                .setVoltage(dto.getVoltage())
                .build();
    }


    private DeviceAddedEventAvro mapToDeviceAddedEventAvro(DeviceAddedEventDto dto) {
        return DeviceAddedEventAvro.newBuilder()
                .setId(dto.getId())
                .setType(DeviceTypeAvro.valueOf(dto.getType().name()))
                .build();
    }

    private DeviceRemovedEventAvro mapToDeviceRemovedEventAvro(DeviceRemovedEventDto dto) {
        return DeviceRemovedEventAvro.newBuilder()
                .setId(dto.getId())
                .build();
    }

    private ScenarioAddedEventAvro mapToScenarioAddedEventAvro(ScenarioAddedEventDto dto) {
        List<ScenarioConditionAvro> avroConditions = dto.getConditions().stream()
                .map(this::mapToScenarioConditionAvro)
                .collect(Collectors.toList());

        List<DeviceActionAvro> avroActions = dto.getActions().stream()
                .map(this::mapToDeviceActionAvro)
                .collect(Collectors.toList());

        return ScenarioAddedEventAvro.newBuilder()
                .setName(dto.getName())
                .setConditions(avroConditions)
                .setActions(avroActions)
                .build();
    }

    private ScenarioRemovedEventAvro mapToScenarioRemovedEventAvro(ScenarioRemovedEventDto dto) {
        return ScenarioRemovedEventAvro.newBuilder()
                .setName(dto.getName())
                .build();
    }

    private ScenarioConditionAvro mapToScenarioConditionAvro(ScenarioConditionDto dto) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ConditionTypeAvro.valueOf(dto.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(dto.getOperation().name()))
                .setValue(dto.getValue())
                .build();
    }

    private DeviceActionAvro mapToDeviceActionAvro(DeviceActionDto dto) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(dto.getSensorId())
                .setType(ActionTypeAvro.valueOf(dto.getType().name()))
                .setValue(dto.getValue())
                .build();
    }
}