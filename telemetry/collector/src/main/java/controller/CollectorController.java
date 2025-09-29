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
import jakarta.validation.Valid;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class CollectorController {

    private final TelemetryService telemetryService;

    @PostMapping("/events/sensors")
    public ResponseEntity<Void> receiveSensorData(@Valid @RequestBody SensorEventDto sensorEvent) {
        telemetryService.send(sensorEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/events/hubs")
    public ResponseEntity<Void> receiveHubEvent(@Valid @RequestBody HubEventDto hubEvent) {
        telemetryService.send(hubEvent);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}