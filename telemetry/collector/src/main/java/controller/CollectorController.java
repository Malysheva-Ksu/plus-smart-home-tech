package controller;

import dto.base.HubEventDto;
import dto.base.SensorEventDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.TelemetryService;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CollectorController {

    private final TelemetryService telemetryService;

    @PostMapping("/sensors/data")
    public ResponseEntity<Void> receiveSensorData(@RequestBody SensorEventDto sensorEvent) {
        telemetryService.send(sensorEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/hubs/events")
    public ResponseEntity<Void> receiveHubEvent(@RequestBody HubEventDto hubEvent) {
        telemetryService.send(hubEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}