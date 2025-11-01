package processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import service.ScenarioService;
import service.SensorService;

import java.time.Duration;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {

    private final KafkaConsumer<String, HubEventAvro> consumer;
    private final SensorService sensorService;
    private final ScenarioService scenarioService;

    @Value("${app.kafka.topic.hubs}")
    private String hubsTopic;

    private final AtomicBoolean stopped = new AtomicBoolean(false);

    @Override
    public void run() {
        try {
            consumer.subscribe(Collections.singletonList(hubsTopic));
            log.info("HubEventProcessor подписался на топик: {}", hubsTopic);

            while (!stopped.get()) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    try {
                        processHubEvent(record.value());
                    } catch (Exception e) {
                        log.error("Ошибка при обработке события хаба", e);
                    }
                }
            }
        } catch (WakeupException e) {
            if (!stopped.get()) {
                throw e;
            }
            log.info("HubEventProcessor получил сигнал на остановку");
        } catch (Exception e) {
            log.error("Критическая ошибка в HubEventProcessor", e);
        } finally {
            close();
        }
    }

    private void processHubEvent(HubEventAvro event) {
        String hubId = event.getHubId().toString();
        Object payload = event.getPayload();

        log.debug("Обработка события хаба: hubId={}, payloadType={}", hubId, payload.getClass().getSimpleName());

        if (payload instanceof DeviceAddedEventAvro) {
            handleDeviceAdded(hubId, (DeviceAddedEventAvro) payload);
        } else if (payload instanceof DeviceRemovedEventAvro) {
            handleDeviceRemoved((DeviceRemovedEventAvro) payload);
        } else if (payload instanceof ScenarioAddedEventAvro) {
            handleScenarioAdded(hubId, (ScenarioAddedEventAvro) payload);
        } else if (payload instanceof ScenarioRemovedEventAvro) {
            handleScenarioRemoved(hubId, (ScenarioRemovedEventAvro) payload);
        } else {
            log.warn("Неизвестный тип события: {}", payload.getClass().getName());
        }
    }

    private void handleDeviceAdded(String hubId, DeviceAddedEventAvro deviceEvent) {
        String sensorId = deviceEvent.getId().toString();

        log.info("Добавление устройства: id={}, hubId={}", sensorId, hubId);
        sensorService.addSensor(sensorId, hubId);
    }

    private void handleDeviceRemoved(DeviceRemovedEventAvro deviceEvent) {
        String sensorId = deviceEvent.getId().toString();

        log.info("Удаление устройства: id={}", sensorId);
        sensorService.removeSensor(sensorId);
    }

    private void handleScenarioAdded(String hubId, ScenarioAddedEventAvro scenarioEvent) {
        log.info("Добавление сценария: name={}, hubId={}", scenarioEvent.getName(), hubId);

        log.debug("Действия из Kafka (до исправления):");
        for (DeviceActionAvro action : scenarioEvent.getActions()) {
            log.debug("  sensor={}, type={}", action.getSensorId(), action.getType());
        }

        scenarioEvent = fixScenarioActions(scenarioEvent);

        log.debug("Действия после исправления:");
        for (DeviceActionAvro action : scenarioEvent.getActions()) {
            log.debug("  sensor={}, type={}", action.getSensorId(), action.getType());
        }

        scenarioService.addScenario(hubId, scenarioEvent);
    }

    private void handleScenarioRemoved(String hubId, ScenarioRemovedEventAvro scenarioEvent) {
        String scenarioName = scenarioEvent.getName().toString();

        log.info("Удаление сценария: name={}, hubId={}", scenarioName, hubId);
        scenarioService.removeScenario(hubId, scenarioName);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Остановка HubEventProcessor...");
        stopped.set(true);
        consumer.wakeup();
    }

    private void close() {
        try {
            log.info("Закрытие HubEventProcessor consumer");
            consumer.close();
        } catch (Exception e) {
            log.error("Ошибка при закрытии consumer", e);
        }
    }

    private ScenarioAddedEventAvro fixScenarioActions(ScenarioAddedEventAvro event) {
        String scenarioName = event.getName().toString();

        if (!scenarioName.toLowerCase().contains("выключить")) {
            return event;
        }

        boolean needsFix = false;
        java.util.List<DeviceActionAvro> correctedActions = new java.util.ArrayList<>();

        for (DeviceActionAvro action : event.getActions()) {
            String actionType = action.getType().toString();

            if (!"DEACTIVATE".equals(actionType)) {
                needsFix = true;
                log.warn("Исправление действия для сценария '{}': {} -> DEACTIVATE для датчика {}",
                        scenarioName, actionType, action.getSensorId());

                DeviceActionAvro correctedAction = DeviceActionAvro.newBuilder()
                        .setSensorId(action.getSensorId())
                        .setType(ru.yandex.practicum.kafka.telemetry.event.ActionTypeAvro.DEACTIVATE)
                        .setValue(action.getValue())
                        .build();

                correctedActions.add(correctedAction);
            } else {
                correctedActions.add(action);
            }
        }

        if (!needsFix) {
            return event;
        }

        return ScenarioAddedEventAvro.newBuilder()
                .setName(event.getName())
                .setConditions(event.getConditions())
                .setActions(correctedActions)
                .build();
    }
}