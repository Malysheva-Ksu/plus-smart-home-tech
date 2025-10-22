package service;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;
    private final ConditionRepository conditionRepository;
    private final ActionRepository actionRepository;
    private final ScenarioConditionRepository scenarioConditionRepository;
    private final ScenarioActionRepository scenarioActionRepository;

    @Transactional
    public void addScenario(String hubId, ScenarioAddedEventAvro event) {
        String scenarioName = event.getName().toString();

        log.info("Добавление сценария: hubId={}, name={}", hubId, scenarioName);

        Optional<Scenario> existing = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        if (existing.isPresent()) {
            log.warn("Сценарий {} для хаба {} уже существует, удаляем старый", scenarioName, hubId);
            scenarioRepository.delete(existing.get());
            scenarioRepository.flush();
        }

        Scenario scenario = new Scenario();
        scenario.setHubId(hubId);
        scenario.setName(scenarioName);
        scenario.setConditions(new ArrayList<>());
        scenario.setActions(new ArrayList<>());
        scenario = scenarioRepository.save(scenario);

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
            Integer value = (valueObj instanceof Integer) ? (Integer) valueObj :
                    Integer.valueOf(valueObj.toString());

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

            scenarioConditionRepository.save(scenarioCondition);
            scenario.getConditions().add(scenarioCondition);
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
            Integer value = (valueObj instanceof Integer) ? (Integer) valueObj :
                    Integer.valueOf(valueObj.toString());

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

            scenarioActionRepository.save(scenarioAction);
            scenario.getActions().add(scenarioAction);
        }

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