package service;

import lombok.extern.slf4j.Slf4j;
import model.Condition;
import model.Scenario;
import model.ScenarioCondition;
import model.enums.ConditionOperation;
import model.enums.ConditionType;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.util.Map;

@Slf4j
@Service
public class ConditionChecker {

    public boolean checkScenarioConditions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        log.debug("Проверка условий сценария {} для хаба {}", scenario.getName(), scenario.getHubId());

        for (ScenarioCondition scenarioCondition : scenario.getConditions()) {
            String sensorId = scenarioCondition.getSensor().getId();
            Condition condition = scenarioCondition.getCondition();

            SensorStateAvro sensorState = sensorsState.get(sensorId);
            if (sensorState == null) {
                log.debug("Датчик {} не найден в снапшоте, условие не выполнено", sensorId);
                return false;
            }

            if (!checkCondition(condition, sensorState)) {
                log.debug("Условие не выполнено для датчика {}", sensorId);
                return false;
            }
        }

        log.info("Все условия сценария {} выполнены", scenario.getName());
        return true;
    }

    private boolean checkCondition(Condition condition, SensorStateAvro sensorState) {
        Object data = sensorState.getData();
        ConditionType type = ConditionType.valueOf(condition.getType());
        ConditionOperation operation = ConditionOperation.valueOf(condition.getOperation());
        int expectedValue = condition.getValue();

        Integer actualValue = extractValue(data, type);
        if (actualValue == null) {
            log.warn("Не удалось извлечь значение типа {} из данных датчика", type);
            return false;
        }

        return compareValues(actualValue, operation, expectedValue);
    }

    private boolean compareValues(int actualValue, ConditionOperation operation, int expectedValue) {
        switch (operation) {
            case EQUALS:
                return actualValue == expectedValue;
            case GREATER_THAN:
                return actualValue > expectedValue;
            case LOWER_THAN:
                return actualValue < expectedValue;
            default:
                log.warn("Неизвестная операция: {}", operation);
                return false;
        }
    }

    private Integer extractValue(Object data, ConditionType type) {
        if (data == null) {
            return null;
        }

        try {
            Object rawValue = null;

            if (data instanceof ru.yandex.practicum.kafka.telemetry.event.ClimateSensorAvro climateData) {

                switch (type) {
                    case TEMPERATURE:
                        rawValue = climateData.getTemperatureC();
                        break;
                    case HUMIDITY:
                        rawValue = climateData.getHumidity();
                        break;
                    case CO2LEVEL:
                        rawValue = climateData.getCo2Level();
                        break;
                    default:
                        log.warn("Неизвестный тип условия {} для ClimateSensorAvro", type);
                        return null;
                }

            } else if (data instanceof ru.yandex.practicum.kafka.telemetry.event.MotionSensorAvro motionData) {

                if (type == ConditionType.MOTION) {
                    Boolean motion = motionData.getMotion();
                    return motion != null ? (motion ? 1 : 0) : null;
                } else {
                    log.warn("Неверный тип условия {} для MotionSensorAvro", type);
                    return null;
                }

            } else if (data instanceof ru.yandex.practicum.kafka.telemetry.event.SwitchSensorAvro switchData) {

                if (type == ConditionType.SWITCH) {
                    Boolean state = switchData.getState();
                    return state != null ? (state ? 1 : 0) : null;
                } else {
                    log.warn("Неверный тип условия {} для SwitchSensorAvro", type);
                    return null;
                }

            } else if (data instanceof ru.yandex.practicum.kafka.telemetry.event.LightSensorAvro lightData) {

                if (type == ConditionType.LUMINOSITY) {
                    rawValue = lightData.getLuminosity();
                } else {
                    log.warn("Неверный тип условия {} для LightSensorAvro", type);
                    return null;
                }

            } else {
                log.error("Неизвестный тип данных датчика: {}", data.getClass().getName());
                return null;
            }

            if (rawValue instanceof Number) {
                return ((Number) rawValue).intValue();
            }

            log.warn("Значение для типа {} не является числом. Фактический тип: {}",
                    type, rawValue != null ? rawValue.getClass().getSimpleName() : "null");
            return null;

        } catch (Exception e) {
            log.error("Ошибка при извлечении значения из payload", e);
            return null;
        }
    }
}