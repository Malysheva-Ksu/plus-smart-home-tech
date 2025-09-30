package controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import service.TelemetryService;

import java.util.Map;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class CollectorController {

    private final TelemetryService telemetryService;

    @PostMapping("/sensors")
    public void addSensorEvent(@RequestBody Map<String, Object> rawEvent) {
        telemetryService.processRawSensorEvent(rawEvent);
    }

    @PostMapping("/hubs")
    public void addHubEvent(@RequestBody Map<String, Object> rawEvent) {
        telemetryService.processRawHubEvent(rawEvent);
    }
}