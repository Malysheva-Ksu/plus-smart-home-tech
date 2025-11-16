package kafka;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.util.Arrays;

public class BaseAvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    protected final Schema schema;
    private final DatumReader<GenericRecord> datumReader;
    private final DecoderFactory decoderFactory;

    public BaseAvroDeserializer(Schema schema) {
        this(DecoderFactory.get(), schema);
    }

    public BaseAvroDeserializer(DecoderFactory decoderFactory, Schema schema) {
        this.decoderFactory = decoderFactory;
        this.schema = schema;
        this.datumReader = new SpecificDatumReader<>(schema);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            BinaryDecoder binaryDecoder = decoderFactory.binaryDecoder(data, null);
            return (T) datumReader.read(null, binaryDecoder);
        } catch (IOException e) {
            throw new SerializationException(
                    "Error deserializing Avro message for topic " + topic + ", data: " + Arrays.toString(data), e
            );
        }
    }
}