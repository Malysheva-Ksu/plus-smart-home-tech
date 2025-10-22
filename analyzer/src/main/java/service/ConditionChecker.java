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

    private Integer extractValue(Object data, ConditionType type) {
        if (data == null) {
            return null;
        }

        try {
            Map<String, Object> payload = (Map<String, Object>) data;

            switch (type) {
                case TEMPERATURE:
                    return (Integer) payload.get("temperature_c");
                case HUMIDITY:
                    return (Integer) payload.get("humidity");
                case CO2LEVEL:
                    return (Integer) payload.get("co2_level");
                case LUMINOSITY:
                    return (Integer) payload.get("luminosity");
                case MOTION:
                    Boolean motion = (Boolean) payload.get("motion");
                    return motion != null ? (motion ? 1 : 0) : null;
                case SWITCH:
                    Boolean state = (Boolean) payload.get("state");
                    return state != null ? (state ? 1 : 0) : null;
                default:
                    log.warn("Неизвестный тип условия: {}", type);
                    return null;
            }
        } catch (Exception e) {
            log.error("Ошибка при извлечении значения из payload", e);
            return null;
        }
    }

    private boolean compareValues(int actualValue, ConditionOperation operation, int expectedValue) {
        switch (operation) {
            case EQUALS:
                return actualValue == expectedValue;
            case GREATER_THAN:
                return actualValue > expectedValue;
            case LESS_THAN:
                return actualValue < expectedValue;
            default:
                log.warn("Неизвестная операция: {}", operation);
                return false;
        }
    }
}