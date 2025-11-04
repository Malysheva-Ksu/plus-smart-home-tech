package service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Action;
import model.Scenario;
import model.ScenarioAction;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc.HubRouterControllerBlockingStub;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerProto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class ActionExecutor {

    @GrpcClient("hub-router")
    private HubRouterControllerBlockingStub hubRouterClient;

    @Value("${app.hubrouter.compensate-enum-mismatch:false}")
    private boolean compensateEnumMismatch;

    @Value("${app.hubrouter.compensate-only-for-scenario:Выключить весь свет}")
    private String compensateOnlyForScenario;

    public void executeScenarioActions(Scenario scenario) {
        String hubId = scenario.getHubId();
        String scenarioName = scenario.getName();

        log.info("Выполнение действий сценария {} для хаба {}", scenarioName, hubId);

        for (ScenarioAction scenarioAction : scenario.getActions()) {
            String sensorId = scenarioAction.getSensor().getId();
            Action action = scenarioAction.getAction();

            try {
                executeAction(hubId, scenarioName, sensorId, action);
            } catch (Exception e) {
                log.error("Ошибка при выполнении действия для датчика {}", sensorId, e);
            }
        }
    }

    private void executeAction(String hubId, String scenarioName, String sensorId, Action action) {
        String originalType = action.getType();
        String actionTypeToSend = originalType;

        if (compensateEnumMismatch && shouldCompensateForScenario(scenarioName)) {
            actionTypeToSend = compensateType(originalType);
            if (!actionTypeToSend.equals(originalType)) {
                log.warn("COMPENSATE_ENUM: сценарий='{}', датчик='{}', оригинал='{}' -> отправляем='{}'",
                        scenarioName, sensorId, originalType, actionTypeToSend);
            } else {
                log.debug("COMPENSATE_ENUM: сценарий='{}', датчик='{}', тип '{}' оставлен без изменений",
                        scenarioName, sensorId, originalType);
            }
        }

        HubRouterControllerProto.DeviceActionProto actionProto =
                HubRouterControllerProto.DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(actionTypeToSend)
                        .setValue(action.getValue() == null ? 0 : action.getValue())
                        .build();

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        HubRouterControllerProto.DeviceActionRequest request =
                HubRouterControllerProto.DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenarioName)
                        .setAction(actionProto)
                        .setTimestamp(timestamp)
                        .build();

        log.info("Отправка команды: hub={}, scenario={}, sensor={}, type={}",
                hubId, scenarioName, sensorId, actionTypeToSend);

        hubRouterClient.handleDeviceAction(request);

        log.info("Команда успешно отправлена");
    }

    private boolean shouldCompensateForScenario(String scenarioName) {
        if (compensateOnlyForScenario == null || compensateOnlyForScenario.isBlank()) {
            return true;
        }
        return compensateOnlyForScenario.equals(scenarioName);
    }

    private String compensateType(String original) {
        if (original == null) return null;
        switch (original) {
            case "ACTIVATE":
                return "DEACTIVATE";
            case "DEACTIVATE":
                return "ACTIVATE";
            default:
                return original;
        }
    }
}