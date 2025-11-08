package service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Action;
import model.Scenario;
import model.ScenarioAction;
import net.devh.boot.grpc.client.inject.GrpcClient;
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
        String actionTypeToSend = action.getType();

        log.info("═══════════════════════════════════════════════════════════");
        log.info("ДИАГНОСТИКА - ОТПРАВКА В HUB-ROUTER:");
        log.info("  actionType из БД: '{}'", actionTypeToSend);
        log.info("  Длина строки: {}", actionTypeToSend.length());
        log.info("═══════════════════════════════════════════════════════════");

        HubRouterControllerProto.ActionTypeProto actionTypeProto = convertToProtoActionType(actionTypeToSend);

        Instant now = Instant.now();
        Timestamp timestamp = Timestamp.newBuilder()
                .setSeconds(now.getEpochSecond())
                .setNanos(now.getNano())
                .build();

        HubRouterControllerProto.DeviceActionProto actionProto =
                HubRouterControllerProto.DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(actionTypeProto)
                        .setValue(action.getValue())
                        .build();

        HubRouterControllerProto.DeviceActionRequest request =
                HubRouterControllerProto.DeviceActionRequest.newBuilder()
                        .setHubId(hubId)
                        .setScenarioName(scenarioName)
                        .setAction(actionProto)
                        .setTimestamp(timestamp)
                        .build();

        log.info("Отправка команды: hub={}, scenario={}, sensor={}, actionType={}",
                hubId, scenarioName, sensorId, actionTypeProto);

        hubRouterClient.handleDeviceAction(request);

        log.info("Команда успешно отправлена");
    }

    private HubRouterControllerProto.ActionTypeProto convertToProtoActionType(String actionType) {
        if (actionType == null) {
            return HubRouterControllerProto.ActionTypeProto.DEACTIVATE;
        }

        String trimmed = actionType.trim().toUpperCase();

        try {
            return HubRouterControllerProto.ActionTypeProto.valueOf(trimmed);
        } catch (IllegalArgumentException e) {
            log.warn("Неизвестный тип действия: '{}', используем DEACTIVATE", actionType);
            return HubRouterControllerProto.ActionTypeProto.DEACTIVATE;
        }
    }
}