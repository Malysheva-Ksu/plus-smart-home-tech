package kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelemetryEventProducer {

    private final KafkaProducer<String, Object> producer;

    @Value("${kafka.topics.sensors}")
    private String sensorTopic;

    @Value("${kafka.topics.hubs}")
    private String hubTopic;

    public void send(SensorEventAvro event) {
        log.info("Preparing to send SensorEventAvro to topic '{}': {}", sensorTopic, event);
        try {
            var record = new ProducerRecord<String, Object>(sensorTopic, event.getId(), event);
            producer.send(record);
            log.info("Successfully sent SensorEventAvro with key '{}'", event.getId());
        } catch (Exception e) {
            log.error("Error sending SensorEventAvro: {}", event, e);
        }
    }

    public void send(HubEventAvro event) {
        log.info("Preparing to send HubEventAvro to topic '{}': {}", hubTopic, event);
        try {
            var record = new ProducerRecord<String, Object>(hubTopic, event.getHubId(), event);
            producer.send(record);
            log.info("Successfully sent HubEventAvro with key '{}'", event.getHubId());
        } catch (Exception e) {
            log.error("Error sending HubEventAvro: {}", event, e);
        }
    }
}