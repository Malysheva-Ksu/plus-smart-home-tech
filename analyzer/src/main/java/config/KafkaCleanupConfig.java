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
    public void cleanKafkaOnStartup() {
        try {
            log.info("Очистка Kafka топика telemetry.actions.v1...");

            kafkaAdminClient.deleteTopics(Collections.singletonList("telemetry.actions.v1"))
                    .all()
                    .get(10, TimeUnit.SECONDS);

            Thread.sleep(2000);

            NewTopic newTopic = new NewTopic("telemetry.actions.v1", 1, (short) 1);
            kafkaAdminClient.createTopics(Collections.singletonList(newTopic))
                    .all()
                    .get(10, TimeUnit.SECONDS);

            log.info("Kafka топик telemetry.actions.v1 очищен и пересоздан");

        } catch (Exception e) {
            log.warn("Не удалось очистить Kafka топик: {}", e.getMessage());
        }
    }
}