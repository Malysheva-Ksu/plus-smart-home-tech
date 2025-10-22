package kafka;

import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.io.IOException;

public class SensorsSnapshotDeserializer implements Deserializer<SensorsSnapshotAvro> {

    private final DatumReader<SensorsSnapshotAvro> datumReader;

    public SensorsSnapshotDeserializer() {
        this.datumReader = new SpecificDatumReader<>(SensorsSnapshotAvro.class);
    }

    @Override
    public SensorsSnapshotAvro deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(data, null);
            return datumReader.read(null, decoder);
        } catch (IOException e) {
            throw new SerializationException(
                    "Error deserializing SensorsSnapshotAvro for topic " + topic, e
            );
        }
    }
}