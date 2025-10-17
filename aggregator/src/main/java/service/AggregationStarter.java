package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {

    private final KafkaConsumer<String, SensorEventAvro> consumer;
    private final KafkaProducer<String, Object> producer;
    private final AggregationService aggregationService;

    @Value("${spring.kafka.consumer.properties.app.kafka.topic.sensors}")
    private String sensorsTopic;
    @Value("${spring.kafka.producer.properties.app.kafka.topic.snapshots}")
    private String snapshotsTopic;

    public void start() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Вызван Shutdown Hook. Инициируем остановку консьюмера");
            consumer.wakeup();
        }));

        try {
            consumer.subscribe(Collections.singletonList(sensorsTopic));
            log.info("Подписались на топик: {}", sensorsTopic);

            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(100));

                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    log.debug("Обрабатываем событие: {}", record.value());
                    aggregationService.updateState(record.value())
                            .ifPresent(snapshot -> {
                                log.info("Отправляем обновленный снапшот для хаба: {}", snapshot.getHubId());
                                producer.send(new ProducerRecord<>(snapshotsTopic, snapshot.getHubId(), snapshot));
                            });
                }
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал на остановку. Завершаем работу.");
        } catch (Exception e) {
            log.error("Ошибка во время обработки событий от датчиков", e);
        } finally {
            try {
                log.info("Сбрасываем буфер продюсера и фиксируем смещения");
                producer.flush();
                consumer.commitSync();
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }
}