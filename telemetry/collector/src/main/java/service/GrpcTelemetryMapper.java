package service;

import com.google.protobuf.Timestamp;
import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import dto.enums.*;
import dto.event.*;
import dto.hub.*;
import dto.nested.*;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.*;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;

import java.time.Instant;
import java.util.stream.Collectors;

@Component
public class GrpcTelemetryMapper {

    public SensorEventDto map(SensorEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case LIGHT_SENSOR_EVENT -> mapToDto(proto.getLightSensorEvent(), proto);
            case TEMPERATURE_SENSOR_EVENT -> mapToDto(proto.getTemperatureSensorEvent(), proto);
            case SWITCH_SENSOR_EVENT -> mapToDto(proto.getSwitchSensorEvent(), proto);
            case CLIMATE_SENSOR_EVENT -> mapToDto(proto.getClimateSensorEvent(), proto);
            case MOTION_SENSOR_EVENT -> mapToDto(proto.getMotionSensorEvent(), proto);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Sensor event payload not set in SensorEventProto");
        };
    }

    public HubEventDto map(HubEventProto proto) {
        return switch (proto.getPayloadCase()) {
            case DEVICE_ADDED -> mapToDto(proto.getDeviceAdded(), proto);
            case DEVICE_REMOVED -> mapToDto(proto.getDeviceRemoved(), proto);
            case SCENARIO_ADDED -> mapToDto(proto.getScenarioAdded(), proto);
            case SCENARIO_REMOVED -> mapToDto(proto.getScenarioRemoved(), proto);
            case PAYLOAD_NOT_SET -> throw new IllegalArgumentException("Hub event payload not set in HubEventProto");
        };
    }

    private LightSensorEventDto mapToDto(LightSensorEvent payload, SensorEventProto parentProto) {
        var dto = new LightSensorEventDto();
        fillCommonSensorFields(dto, parentProto);
        dto.setLuminosity(payload.getLuminosity());
        dto.setLinkQuality(payload.getLinkQuality());
        return dto;
    }

    private TemperatureSensorEventDto mapToDto(TemperatureSensorEvent payload, SensorEventProto parentProto) {
        var dto = new TemperatureSensorEventDto();
        fillCommonSensorFields(dto, parentProto);
        dto.setTemperatureC(payload.getTemperatureC());
        return dto;
    }

    private SwitchSensorEventDto mapToDto(SwitchSensorEvent payload, SensorEventProto parentProto) {
        var dto = new SwitchSensorEventDto();
        fillCommonSensorFields(dto, parentProto);
        dto.setState(payload.getState());
        return dto;
    }

    private ClimateSensorEventDto mapToDto(ClimateSensorEvent payload, SensorEventProto parentProto) {
        var dto = new ClimateSensorEventDto();
        fillCommonSensorFields(dto, parentProto);
        dto.setTemperatureC(payload.getTemperatureC());
        dto.setHumidity(payload.getHumidity());
        dto.setCo2Level(payload.getCo2Level());
        return dto;
    }

    private MotionSensorEventDto mapToDto(MotionSensorEvent payload, SensorEventProto parentProto) {
        var dto = new MotionSensorEventDto();
        fillCommonSensorFields(dto, parentProto);
        dto.setMotion(payload.getMotion());
        dto.setVoltage(payload.getVoltage());
        dto.setLinkQuality(payload.getLinkQuality());
        return dto;
    }

    private DeviceAddedEventDto mapToDto(DeviceAddedEventProto payload, HubEventProto parentProto) {
        var dto = new DeviceAddedEventDto();
        fillCommonHubFields(dto, parentProto);
        dto.setId(payload.getId());
        dto.setType(DeviceTypeDto.valueOf(payload.getType().name()));
        return dto;
    }

    private DeviceRemovedEventDto mapToDto(DeviceRemovedEventProto payload, HubEventProto parentProto) {
        var dto = new DeviceRemovedEventDto();
        fillCommonHubFields(dto, parentProto);
        dto.setId(payload.getId());
        return dto;
    }

    private ScenarioAddedEventDto mapToDto(ScenarioAddedEventProto payload, HubEventProto parentProto) {
        var dto = new ScenarioAddedEventDto();
        fillCommonHubFields(dto, parentProto);
        dto.setName(payload.getName());
        dto.setConditions(payload.getConditionList().stream().map(this::mapToDto).collect(Collectors.toList()));
        dto.setActions(payload.getActionList().stream().map(this::mapToDto).collect(Collectors.toList()));
        return dto;
    }

    private ScenarioRemovedEventDto mapToDto(ScenarioRemovedEventProto payload, HubEventProto parentProto) {
        var dto = new ScenarioRemovedEventDto();
        fillCommonHubFields(dto, parentProto);
        dto.setName(payload.getName());
        return dto;
    }

    private ScenarioConditionDto mapToDto(ScenarioConditionProto proto) {
        var dto = new ScenarioConditionDto();
        dto.setSensorId(proto.getSensorId());
        dto.setType(ConditionTypeDto.valueOf(proto.getType().name()));
        dto.setOperation(ConditionOperationDto.valueOf(proto.getOperation().name()));
        dto.setValue(extractConditionValue(proto));
        return dto;
    }

    private DeviceActionDto mapToDto(DeviceActionProto proto) {
        var dto = new DeviceActionDto();
        dto.setSensorId(proto.getSensorId());
        dto.setType(ActionTypeDto.valueOf(proto.getType().name()));
        dto.setValue(proto.getValue());
        return dto;
    }

    private void fillCommonSensorFields(SensorEventDto dto, SensorEventProto parentProto) {
        dto.setId(parentProto.getId());
        dto.setHubId(parentProto.getHubId());
        dto.setTimestamp(convertTimestamp(parentProto.getTimestamp()));
    }

    private void fillCommonHubFields(HubEventDto dto, HubEventProto parentProto) {
        dto.setHubId(parentProto.getHubId());
        dto.setTimestamp(convertTimestamp(parentProto.getTimestamp()));
    }

    private Instant convertTimestamp(Timestamp timestamp) {
        if (timestamp == null || (timestamp.getSeconds() == 0 && timestamp.getNanos() == 0)) {
            return null;
        }
        return Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos());
    }

    private Object extractConditionValue(ScenarioConditionProto proto) {
        switch (proto.getValueCase()) {
            case BOOL_VALUE:
                return proto.getBoolValue();
            case INT_VALUE:
                return proto.getIntValue();
            case VALUE_NOT_SET:
                return null;
            default:
                throw new IllegalArgumentException("Unknown value type in ScenarioConditionProto: " + proto.getValueCase());
        }
    }
}