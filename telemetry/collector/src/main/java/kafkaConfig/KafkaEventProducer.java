package kafkaConfig;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaEventProducer {

    private final KafkaProducer<String, byte[]> kafkaProducer;

    @Value("${kafka.topics.sensors}")
    private String sensorsTopic;

    @Value("${kafka.topics.hubs}")
    private String hubsTopic;

    public void send(SensorEventAvro event) {
        byte[] eventBytes = serializeAvro(event);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(sensorsTopic, event.getId().toString(), eventBytes);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.debug("SensorEventAvro отправлен: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            } else {
                log.error("Ошибка отправки SensorEventAvro", exception);
            }
        });
    }

    public void send(HubEventAvro event) {
        byte[] eventBytes = serializeAvro(event);
        ProducerRecord<String, byte[]> record = new ProducerRecord<>(hubsTopic, event.getHubId().toString(), eventBytes);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception == null) {
                log.debug("HubEventAvro отправлен: topic={}, partition={}, offset={}",
                        metadata.topic(), metadata.partition(), metadata.offset());
            } else {
                log.error("Ошибка отправки HubEventAvro", exception);
            }
        });
    }

    private <T extends SpecificRecordBase> byte[] serializeAvro(T record) {
        SpecificDatumWriter<T> datumWriter = new SpecificDatumWriter<>(record.getSchema());
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            datumWriter.write(record, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сериализации Avro-объекта: " + e.getMessage(), e);
        }
    }
}