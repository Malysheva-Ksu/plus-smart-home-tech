package config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaCleanupConfig {

    private final AdminClient kafkaAdminClient;

    @EventListener(ApplicationReadyEvent.class)
    public void cleanAllKafkaTopics() {
        try {
            String[] topicsToClean = {
                    "telemetry.actions.v1",
                    "telemetry.sensors.v1",
                    "telemetry.snapshots.v1",
                    "telemetry.hubs.v1"
            };

            for (String topic : topicsToClean) {
                try {
                    kafkaAdminClient.deleteTopics(Collections.singletonList(topic))
                            .all()
                            .get(10, TimeUnit.SECONDS);
                    Thread.sleep(1000);

                    NewTopic newTopic = new NewTopic(topic, 1, (short) 1);
                    kafkaAdminClient.createTopics(Collections.singletonList(newTopic))
                            .all()
                            .get(10, TimeUnit.SECONDS);

                    log.info("Очищен и пересоздан топик: {}", topic);

                } catch (Exception e) {
                    log.info("Топик {} не существует: {}", topic, e.getMessage());
                }
            }

        } catch (Exception e) {
            log.warn("Ошибка очистки Kafka: {}", e.getMessage());
        }
    }
}