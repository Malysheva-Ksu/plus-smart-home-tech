package service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Sensor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repository.SensorRepository;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SensorService {

    private final SensorRepository sensorRepository;

    @Transactional
    public void addSensor(String sensorId, String hubId, String deviceType) {
        Optional<Sensor> existing = sensorRepository.findById(sensorId);
        if (existing.isPresent()) {
            log.debug("Датчик {} уже существует для хаба {}", sensorId, hubId);
            return;
        }

        Sensor sensor = new Sensor(sensorId, hubId);
        sensor.setDeviceType(deviceType);
        sensorRepository.save(sensor);
        log.info("Добавлен датчик: id={}, hubId={}, type={}",
                sensorId, hubId, deviceType);
    }

    @Transactional
    public void removeSensor(String sensorId) {
        if (sensorRepository.existsById(sensorId)) {
            sensorRepository.deleteById(sensorId);
            log.info("Удалён датчик: id={}", sensorId);
        } else {
            log.warn("Попытка удалить несуществующий датчик: id={}", sensorId);
        }
    }

    @Transactional(readOnly = true)
    public Optional<Sensor> getSensor(String sensorId) {
        return sensorRepository.findById(sensorId);
    }
}