package kafka.consumer;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import ru.yandex.practicum.kafka.telemetry.event.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class KafkaConsumerApplication {

    private static final String SERVER = "localhost:9092";
    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";
    private static final String GROUP_ID = "telemetry-reader-group";

    private static final SpecificDatumReader<SensorEventAvro> sensorReader = new SpecificDatumReader<>(SensorEventAvro.class);
    private static final SpecificDatumReader<HubEventAvro> hubReader = new SpecificDatumReader<>(HubEventAvro.class);

    public static void main(String[] args) {
        Properties config = getConsumerProperties();

        try (KafkaConsumer<String, byte[]> consumer = new KafkaConsumer<>(config)) {
            consumer.subscribe(Arrays.asList(SENSORS_TOPIC, HUBS_TOPIC));
            System.out.println("Consumer запущен. Ожидание сообщений...");

            while (true) {
                ConsumerRecords<String, byte[]> records = consumer.poll(Duration.ofMillis(1000));
                records.forEach(record -> {
                    System.out.printf("Получено сообщение из топика: %s, key=%s\n", record.topic(), record.key());
                    byte[] data = record.value();

                    if (record.topic().equals(SENSORS_TOPIC)) {
                        try {
                            SensorEventAvro event = deserializeAvro(data, sensorReader);
                            handleSensorEvent(event);
                        } catch (IOException e) {
                            System.err.println("Не удалось десериализовать SensorEventAvro: " + e.getMessage());
                        }
                    } else if (record.topic().equals(HUBS_TOPIC)) {
                        try {
                            HubEventAvro event = deserializeAvro(data, hubReader);
                            handleHubEvent(event);
                        } catch (IOException e) {
                            System.err.println("Не удалось десериализовать HubEventAvro: " + e.getMessage());
                        }
                    }
                });
            }
        }
    }

    private static <T extends SpecificRecordBase> T deserializeAvro(byte[] data, SpecificDatumReader<T> reader) throws IOException {
        BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
        return reader.read(null, decoder);
    }

    private static void handleSensorEvent(SensorEventAvro event) {
        Object payload = event.getPayload();
        if (payload instanceof TemperatureSensorAvro) {
            System.out.printf("\t[Сенсор] ID: %s, Температура: %d°C\n\n", event.getId(), ((TemperatureSensorAvro) payload).getTemperatureC());
        } else {
            System.out.printf("\t[Сенсор] ID: %s, Неизвестный тип payload: %s\n\n", event.getId(), payload.getClass().getSimpleName());
        }
    }

    private static void handleHubEvent(HubEventAvro event) {
        Object payload = event.getPayload();
        if (payload instanceof DeviceAddedEventAvro) {
            System.out.printf("\t[Хаб] ID: %s, Добавлено устройство типа: %s\n\n", event.getHubId(), ((DeviceAddedEventAvro) payload).getType());
        } else {
            System.out.printf("\t[Хаб] ID: %s, Неизвестный тип payload: %s\n\n", event.getHubId(), payload.getClass().getSimpleName());
        }
    }

    private static Properties getConsumerProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, SERVER);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, GROUP_ID);
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class.getName());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return properties;
    }
}