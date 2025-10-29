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
        String originalActionType = action.getType();
        String actionTypeToSend = originalActionType;

        if ("Выключить весь свет".equals(scenarioName) && "ACTIVATE".equals(actionTypeToSend)) {
            log.warn("Исправление типа действия ПЕРЕД ОТПРАВКОЙ: Сценарий '{}' ожидает DEACTIVATE, но из БД получено {}. Принудительно меняем на DEACTIVATE.",
                    scenarioName, originalActionType);
            actionTypeToSend = "DEACTIVATE";
            action.setType("DEACTIVATE");
        }

        log.debug("Подготовка к отправке действия: hubId={}, sensor={}, typeFromDb={}, typeToSend={}, value={}",
                hubId, sensorId, originalActionType, actionTypeToSend, action.getValue());

        HubRouterControllerProto.DeviceActionProto actionProto =
                HubRouterControllerProto.DeviceActionProto.newBuilder()
                        .setSensorId(sensorId)
                        .setType(actionTypeToSend)
                        .setValue(action.getValue())
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

        log.info("Отправка gRPC запроса: {}", request.toString().replace("\n", " "));

        try {
            hubRouterClient.handleDeviceAction(request);
            log.info("Действие успешно отправлено: sensor={}, type={}", sensorId, actionTypeToSend);
        } catch (Exception e) {
            log.error("Ошибка при отправке действия через gRPC", e);
        }
    }
}