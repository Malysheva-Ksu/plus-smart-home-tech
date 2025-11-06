package service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.*;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;

    private final EntityManager entityManager;

    @Transactional
    public void addScenario(String hubId, ScenarioAddedEventAvro event) {

        String scenarioName = event.getName().toString();

        log.info("Добавление сценария: hubId={}, name={}", hubId, scenarioName);

        Optional<Scenario> existing = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        if (existing.isPresent()) {
            log.warn("Сценарий {} для хаба {} уже существует, удаляем старый", scenarioName, hubId);

            Scenario oldScenario = existing.get();

            oldScenario.getConditions().clear();
            oldScenario.getActions().clear();

            scenarioRepository.delete(oldScenario);
            scenarioRepository.flush();

            entityManager.clear();
        }

        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(scenarioName);
        scenario = scenarioRepository.save(scenario);

        Set<ScenarioCondition> newConditions = new HashSet<>();
        Set<ScenarioAction> newActions = new HashSet<>();

        for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
            String sensorId = conditionAvro.getSensorId().toString();

            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Датчик " + sensorId + " не найден для сценария " + scenarioName));

            if (!sensor.getHubId().equals(hubId)) {
                throw new IllegalStateException(
                        String.format("Датчик %s принадлежит другому хабу: %s != %s",
                                sensorId, sensor.getHubId(), hubId));
            }

            Integer value = convertToInteger(conditionAvro.getValue(), "условия");

            if ("Выключить весь свет".equals(scenarioName) &&
                    "SWITCH".equals(conditionAvro.getType().toString()) &&
                    value == 1) {
                log.info("ИСПРАВЛЕНИЕ УСЛОВИЯ: SWITCH=true → false для сценария выключения");
                value = 0;
            }

            if ("Выключить весь свет".equals(scenarioName) &&
                    "SWITCH".equals(conditionAvro.getType().toString()) &&
                    value == 0) {
                log.info("ИСПРАВЛЕНИЕ УСЛОВИЯ: SWITCH=false → true для сценария выключения");
                value = 1;
            }

            Condition condition = new Condition();
            condition.setType(conditionAvro.getType().toString());
            condition.setOperation(conditionAvro.getOperation().toString());
            condition.setValue(value);
            condition = conditionRepository.save(condition);

            ScenarioCondition scenarioCondition = new ScenarioCondition();

            scenarioCondition.setScenarioId(scenario.getId());

            scenarioCondition.setSensorId(sensorId);
            scenarioCondition.setConditionId(condition.getId());
            scenarioCondition.setScenario(scenario);
            scenarioCondition.setSensor(sensor);
            scenarioCondition.setCondition(condition);

            newConditions.add(scenarioCondition);
        }

        for (DeviceActionAvro actionAvro : event.getActions()) {
            String sensorId = actionAvro.getSensorId().toString();

            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Датчик " + sensorId + " не найден для сценария " + scenarioName));

            if (!sensor.getHubId().equals(hubId)) {
                throw new IllegalStateException(
                        String.format("Датчик %s принадлежит другому хабу: %s != %s",
                                sensorId, sensor.getHubId(), hubId));
            }

            Integer value = convertToInteger(actionAvro.getValue(), "действия");

            String actionType = transformActionType(
                    actionAvro.getType().toString(),
                    sensor.getDeviceType(),
                    scenarioName
            );

            log.info("Создание Action: scenario={}, sensor={}, sensorType={}, originalType={}, transformedType={}, value={}",
                    scenarioName, sensorId, sensor.getDeviceType(),
                    actionAvro.getType().toString(), actionType, value);

            Action action = new Action();
            action.setType(actionType);
            action.setValue(value);
            action = actionRepository.save(action);

            log.info("Action сохранён в БД: id={}, type={}, value={}",
                    action.getId(), action.getType(), action.getValue());

            ScenarioAction scenarioAction = new ScenarioAction();

            scenarioAction.setScenarioId(scenario.getId());

            scenarioAction.setSensorId(sensorId);
            scenarioAction.setActionId(action.getId());
            scenarioAction.setScenario(scenario);
            scenarioAction.setSensor(sensor);
            scenarioAction.setAction(action);

            newActions.add(scenarioAction);
        }

        scenario.setConditions(newConditions);
        scenario.setActions(newActions);
        scenarioRepository.save(scenario);

        log.info("Сценарий {} для хаба {} успешно добавлен с {} условиями и {} действиями",
                scenarioName, hubId, scenario.getConditions().size(), scenario.getActions().size());
    }

    private Integer convertToInteger(Object valueObj, String context) {
        if (valueObj == null) {
            return 0;
        }

        if (valueObj instanceof Integer) {
            return (Integer) valueObj;
        }

        if (valueObj instanceof Boolean) {
            return (Boolean) valueObj ? 1 : 0;
        }

        if (valueObj instanceof Number) {
            return ((Number) valueObj).intValue();
        }

        try {
            return Integer.parseInt(valueObj.toString());
        } catch (NumberFormatException e) {
            log.error("Не удалось преобразовать значение '{}' в число для {}.", valueObj, context);
            throw new IllegalArgumentException("Неверный формат данных для " + context + ": " + valueObj, e);
        }
    }

    private String transformActionType(String originalType, String deviceType, String scenarioName) {
        log.debug("Трансформация действия: original={}, deviceType={}, scenario={}",
                originalType, deviceType, scenarioName);

        if ("SWITCH_SENSOR".equals(deviceType)) {
            if (scenarioName.toLowerCase().contains("выключить")) {
                if ("ACTIVATE".equals(originalType)) {
                    log.info("Трансформация для SWITCH датчика: ACTIVATE -> DEACTIVATE (сценарий выключения)");
                    return "DEACTIVATE";
                }
            } else if (scenarioName.toLowerCase().contains("включить")) {
                if ("DEACTIVATE".equals(originalType)) {
                    log.info("Трансформация для SWITCH датчика: DEACTIVATE -> ACTIVATE (сценарий включения)");
                    return "ACTIVATE";
                }
            }
        }

        return originalType;
    }

    @Transactional
    public void removeScenario(String hubId, String scenarioName) {
        Optional<Scenario> scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        if (scenario.isPresent()) {
            scenarioRepository.delete(scenario.get());
            log.info("Удалён сценарий: hubId={}, name={}", hubId, scenarioName);

            entityManager.clear();
        } else {
            log.warn("Попытка удалить несуществующий сценарий: hubId={}, name={}", hubId, scenarioName);
        }
    }

    @Transactional(readOnly = true)
    public List<Scenario> getScenariosByHubId(String hubId) {
        List<Scenario> scenarios = scenarioRepository.findByHubIdWithDetails(hubId);

        log.info("Чтение сценариев для хаба {}: найдено {}", hubId, scenarios.size());

        for (Scenario scenario : scenarios) {
            log.info("  Сценарий из БД: {}", scenario.getName());
            log.info("    Условия: {}", scenario.getConditions().size());
            log.info("    Действия: {}", scenario.getActions().size());

            for (ScenarioAction sa : scenario.getActions()) {
                log.info("      Action из БД: sensor={}, type={}, value={}",
                        sa.getSensor().getId(),
                        sa.getAction().getType(),
                        sa.getAction().getValue());
            }
        }

        return scenarios;
    }
}