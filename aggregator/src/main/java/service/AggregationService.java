package service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorStateAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class AggregationService {

    private final Map<String, SensorsSnapshotAvro> snapshots = new ConcurrentHashMap<>();

    public Optional<SensorsSnapshotAvro> updateState(SensorEventAvro event) {
        SensorsSnapshotAvro snapshot = snapshots.computeIfAbsent(
                event.getHubId(),
                hubId -> new SensorsSnapshotAvro(hubId, Instant.EPOCH, new ConcurrentHashMap<>())
        );

        String deviceId = event.getId();
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();
        SensorStateAvro oldState = sensorsState.get(deviceId);

        if (oldState != null) {
            if (!oldState.getTimestamp().isBefore(event.getTimestamp()) || Objects.equals(oldState.getData(), event.getPayload())) {
                return Optional.empty();
            }
        }

        SensorStateAvro newState = new SensorStateAvro(event.getTimestamp(), event.getPayload());

        sensorsState.put(deviceId, newState);
        snapshot.setTimestamp(event.getTimestamp());

        return Optional.of(snapshot);
    }
}