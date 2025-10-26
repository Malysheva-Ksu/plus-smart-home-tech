package service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.ActionType;
import model.enums.ConditionOperation;
import model.enums.ConditionType;
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

            Object valueObj = conditionAvro.getValue();
            Integer value;

            if (valueObj instanceof Integer) {
                value = (Integer) valueObj;
            } else if (valueObj instanceof Long) {
                value = ((Long) valueObj).intValue();
            } else if (valueObj instanceof Boolean) {
                value = (Boolean) valueObj ? 1 : 0;
            } else {
                try {
                    value = Integer.valueOf(valueObj.toString());
                } catch (NumberFormatException e) {
                    log.error("Не удалось преобразовать значение '{}' в число для условия.", valueObj.toString());
                    throw new IllegalArgumentException("Неверный формат данных для условия: " + valueObj.toString(), e);
                }
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

            Object valueObj = actionAvro.getValue();
            Integer value;

            if (valueObj instanceof Integer) {
                value = (Integer) valueObj;
            } else if (valueObj instanceof Long) {
                value = ((Long) valueObj).intValue();
            } else if (valueObj instanceof Boolean) {
                value = (Boolean) valueObj ? 1 : 0;
            } else {
                try {
                    value = Integer.valueOf(valueObj.toString());
                } catch (NumberFormatException e) {
                    log.error("Не удалось преобразовать значение '{}' в число для действия.", valueObj.toString());
                    throw new IllegalArgumentException("Неверный формат данных для действия: " + valueObj.toString(), e);
                }
            }

            Action action = new Action();
            action.setType(actionAvro.getType().toString());
            action.setValue(value);
            action = actionRepository.save(action);

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

    @Transactional
    public void removeScenario(String hubId, String scenarioName) {
        Optional<Scenario> scenario = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        if (scenario.isPresent()) {
            scenarioRepository.delete(scenario.get());
            log.info("Удалён сценарий: hubId={}, name={}", hubId, scenarioName);
        } else {
            log.warn("Попытка удалить несуществующий сценарий: hubId={}, name={}", hubId, scenarioName);
        }
    }

    @Transactional(readOnly = true)
    public List<Scenario> getScenariosByHubId(String hubId) {
        return scenarioRepository.findByHubIdWithDetails(hubId);
    }
}