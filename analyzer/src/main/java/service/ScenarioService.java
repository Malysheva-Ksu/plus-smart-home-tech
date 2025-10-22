package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.ActionType;
import model.enums.ConditionOperation;
import model.enums.ConditionType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.ScenarioRepository;
import repository.SensorRepository;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.ScenarioConditionAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceActionAvro;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final SensorRepository sensorRepository;

    @Transactional
    public void addScenario(String hubId, ScenarioAddedEventAvro event) {
        String scenarioName = event.getName().toString();

        log.info("Добавление сценария: hubId={}, name={}", hubId, scenarioName);

        Optional<Scenario> existing = scenarioRepository.findByHubIdAndName(hubId, scenarioName);
        if (existing.isPresent()) {
            log.warn("Сценарий {} для хаба {} уже существует, удаляем старый", scenarioName, hubId);
            scenarioRepository.delete(existing.get());
        }

        Scenario scenario = new Scenario(hubId, scenarioName);
        scenario = scenarioRepository.save(scenario);

        for (ScenarioConditionAvro conditionAvro : event.getConditions()) {
            String sensorId = conditionAvro.getSensorId().toString();

            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Датчик " + sensorId + " не найден для сценария " + scenarioName));

            Object valueObj = conditionAvro.getValue();
            Integer value = (valueObj instanceof Integer) ? (Integer) valueObj :
                    Integer.valueOf(valueObj.toString());

            Condition condition = new Condition(
                    ConditionType.valueOf(conditionAvro.getType().toString()),
                    ConditionOperation.valueOf(conditionAvro.getOperation().toString()),
                    value
            );

            ScenarioCondition scenarioCondition = new ScenarioCondition();
            scenarioCondition.setScenario(scenario);
            scenarioCondition.setSensor(sensor);
            scenarioCondition.setCondition(condition);

            scenario.getConditions().add(scenarioCondition);
        }

        for (DeviceActionAvro actionAvro : event.getActions()) {
            String sensorId = actionAvro.getSensorId().toString();

            Sensor sensor = sensorRepository.findById(sensorId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Датчик " + sensorId + " не найден для сценария " + scenarioName));

            Object valueObj = actionAvro.getValue();
            Integer value = (valueObj instanceof Integer) ? (Integer) valueObj :
                    Integer.valueOf(valueObj.toString());

            Action action = new Action(
                    ActionType.valueOf(actionAvro.getType().toString()),
                    value
            );

            ScenarioAction scenarioAction = new ScenarioAction();
            scenarioAction.setScenario(scenario);
            scenarioAction.setSensor(sensor);
            scenarioAction.setAction(action);

            scenario.getActions().add(scenarioAction);
        }

        scenarioRepository.save(scenario);
        log.info("Сценарий {} для хаба {} успешно добавлен", scenarioName, hubId);
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