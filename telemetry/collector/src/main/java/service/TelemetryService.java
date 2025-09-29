package service;

import dto.base.HubEventDto;
import dto.base.SensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

@Service
@RequiredArgsConstructor
public class TelemetryService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final TelemetryMapper mapper;

    private static final String SENSORS_TOPIC = "telemetry.sensors.v1";
    private static final String HUBS_TOPIC = "telemetry.hubs.v1";

    public void send(SensorEventDto dto) {
        String key = dto.getId();
        SensorEventAvro avroEvent = mapper.toAvro(dto);
        kafkaTemplate.send(SENSORS_TOPIC, key, avroEvent);
    }

    public void send(HubEventDto dto) {
        String key = dto.getHubId();
        HubEventAvro avroEvent = mapper.toAvro(dto);
        kafkaTemplate.send(HUBS_TOPIC, key, avroEvent);
    }
}