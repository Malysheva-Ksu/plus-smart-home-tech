package kafka.producer;

import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Properties;
import java.util.UUID;

public class KafkaProducerApplication {

    private static final String SERVER = "localhost:9092";
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    public static void main(String[] args) throws InterruptedException {
        Properties config = getProducerProperties();

        try (KafkaProducer<String, byte[]> producer = new KafkaProducer<>(config)) {
            System.out.println("Producer запущен.");

            for (int i = 0; i < 3; i++) {
                String sensorId = "sensor-" + i;
                SensorEventAvro sensorEvent = createTemperatureEvent(sensorId);
                byte[] sensorEventBytes = serializeAvro(sensorEvent);
                ProducerRecord<String, byte[]> sensorRecord = new ProducerRecord<>(SENSORS_TOPIC, sensorId, sensorEventBytes);
                producer.send(sensorRecord, (metadata, e) -> {
                    if (e == null) {
                        System.out.printf("Событие сенсора отправлено в топик %s\n", metadata.topic());
                    } else {
                        e.printStackTrace();
                    }
                });
                Thread.sleep(500);

                String hubId = "hub-" + i;
                HubEventAvro hubEvent = createDeviceAddedEvent(hubId);
                byte[] hubEventBytes = serializeAvro(hubEvent);
                ProducerRecord<String, byte[]> hubRecord = new ProducerRecord<>(HUBS_TOPIC, hubId, hubEventBytes);
                producer.send(hubRecord, (metadata, e) -> {
                    if (e == null) {
                        System.out.printf("Событие хаба отправлено в топик %s\n", metadata.topic());
                    } else {
                        e.printStackTrace();
                    }
                });
                Thread.sleep(500);
            }
            producer.flush();
        }
    }

    private static <T extends SpecificRecordBase> byte[] serializeAvro(T record) {
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

    private static Properties getProducerProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        properties.put(ProducerConfig.CLIENT_ID_CONFIG, "telemetry-producer");
        properties.put(ProducerConfig.ACKS_CONFIG, "all");
        return properties;
    }

    private static SensorEventAvro createTemperatureEvent(String sensorId) {
        TemperatureSensorAvro tempSensor = TemperatureSensorAvro.newBuilder()
                .setId(sensorId).setHubId("hub-1").setTimestamp(Instant.now()).setTemperatureC(25).setTemperatureF(77).build();
        return SensorEventAvro.newBuilder()
                .setId(sensorId).setHubId("hub-1").setTimestamp(Instant.now()).setPayload(tempSensor).build();
    }

    private static HubEventAvro createDeviceAddedEvent(String hubId) {
        DeviceAddedEventAvro deviceAdded = DeviceAddedEventAvro.newBuilder()
                .setId(UUID.randomUUID().toString()).setType(DeviceTypeAvro.MOTION_SENSOR).build();
        return HubEventAvro.newBuilder()
                .setHubId(hubId).setTimestamp(Instant.now()).setPayload(deviceAdded).build();
    }
}