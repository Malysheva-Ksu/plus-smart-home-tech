package processor;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Scenario;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import service.ActionExecutor;
import service.ConditionChecker;
import service.ScenarioService;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor {

    private final KafkaConsumer<String, SensorsSnapshotAvro> consumer;
    private final ScenarioService scenarioService;
    private final ConditionChecker conditionChecker;
    private final ActionExecutor actionExecutor;

    @Value("${app.kafka.topic.snapshots}")
    private String snapshotsTopic;

    private final AtomicBoolean stopped = new AtomicBoolean(false);

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Вызван Shutdown Hook. Инициируем остановку SnapshotProcessor");
            shutdown();
        }));

        try {
            consumer.subscribe(Collections.singletonList(snapshotsTopic));
            log.info("SnapshotProcessor подписался на топик: {}", snapshotsTopic);

            while (!stopped.get()) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(1000));

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        processSnapshot(record.value());
                    } catch (Exception e) {
                        log.error("Ошибка при обработке снапшота", e);
                    }
                }
            }
        } catch (WakeupException e) {
            if (!stopped.get()) {
                throw e;
            }
            log.info("SnapshotProcessor получил сигнал на остановку");
        } catch (Exception e) {
            log.error("Критическая ошибка в SnapshotProcessor", e);
        } finally {
            close();
        }
    }

    private void processSnapshot(SensorsSnapshotAvro snapshot) {
        String hubId = snapshot.getHubId().toString();
        log.debug("Обработка снапшота для хаба: {}, timestamp={}", hubId, snapshot.getTimestamp());

        List<Scenario> scenarios = scenarioService.getScenariosByHubId(hubId);

        if (scenarios.isEmpty()) {
            log.debug("Нет сценариев для хаба {}", hubId);
            return;
        }

        log.debug("Найдено {} сценариев для хаба {}", scenarios.size(), hubId);

        for (Scenario scenario : scenarios) {
            try {
                if (conditionChecker.checkScenarioConditions(scenario, snapshot)) {
                    log.info("Сценарий {} активирован для хаба {}", scenario.getName(), hubId);

                    actionExecutor.executeScenarioActions(scenario);
                } else {
                    log.debug("Условия сценария {} не выполнены", scenario.getName());
                }
            } catch (Exception e) {
                log.error("Ошибка при обработке сценария {}", scenario.getName(), e);
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        log.info("Остановка SnapshotProcessor...");
        stopped.set(true);
        consumer.wakeup();
    }

    private void close() {
        try {
            log.info("Закрытие SnapshotProcessor consumer");
            consumer.close();
        } catch (Exception e) {
            log.error("Ошибка при закрытии consumer", e);
        }
    }
}