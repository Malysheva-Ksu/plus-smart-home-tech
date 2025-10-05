package service;

import dto.abstractDto.HubEventDto;
import dto.abstractDto.SensorEventDto;
import kafkaConfig.KafkaEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryServiceImpl implements TelemetryService {

    private final KafkaEventProducer eventProducer;
    private final TelemetryMapper telemetryMapper;
    private final EventTypeResolver eventTypeResolver;

    @Override
    public void save(SensorEventDto event) {
        var avro = telemetryMapper.toAvro(event);
        sendAndLog(avro, event, "Sensor");
    }

    @Override
    public void save(HubEventDto event) {
        var avro = telemetryMapper.toAvro(event);
        sendAndLog(avro, event, "Hub");
    }

    @Override
    public void processRawSensorEvent(Map<String, Object> rawEvent) {
        log.info("Processing raw sensor event: {}", rawEvent);
        SensorEventDto eventDto = eventTypeResolver.resolveSensorEvent(rawEvent);
        save(eventDto);
    }

    @Override
    public void processRawHubEvent(Map<String, Object> rawEvent) {
        log.info("Processing raw hub event: {}", rawEvent);
        HubEventDto eventDto = eventTypeResolver.resolveHubEvent(rawEvent);
        save(eventDto);
    }

    private void sendAndLog(Object avroEvent, Object dto, String eventType) {
        if (avroEvent instanceof SensorEventAvro) {
            eventProducer.send((SensorEventAvro) avroEvent);
        } else if (avroEvent instanceof HubEventAvro) {
            eventProducer.send((HubEventAvro) avroEvent);
        } else {
            throw new IllegalStateException("Unknown avro event type: " + avroEvent.getClass().getName());
        }
        log.info("{} event saved: {}", eventType, dto);
    }
}