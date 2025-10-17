package controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.controller.CollectorControllerGrpc.CollectorControllerImplBase;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import service.GrpcTelemetryMapper;
import service.TelemetryService;

@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcEventController extends CollectorControllerImplBase {

    private final TelemetryService telemetryService;
    private final GrpcTelemetryMapper grpcTelemetryMapper;

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Processing gRPC sensor event of type: {}", request.getPayloadCase());
            var sensorEventDto = grpcTelemetryMapper.map(request);
            telemetryService.save(sensorEventDto);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error processing gRPC sensor event", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        try {
            log.info("Processing gRPC hub event of type: {}", request.getPayloadCase());
            var hubEventDto = grpcTelemetryMapper.map(request);
            telemetryService.save(hubEventDto);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error processing gRPC hub event", e);
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getMessage())
                            .withCause(e)
            ));
        }
    }
}