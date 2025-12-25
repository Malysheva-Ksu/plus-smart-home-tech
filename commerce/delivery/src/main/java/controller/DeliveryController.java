package controller;

import model.delivery.CreateNewDeliveryRequest;
import model.delivery.DeliveryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import service.DeliveryService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/delivery")
public class DeliveryController {

    private static final Logger log = LoggerFactory.getLogger(DeliveryController.class);

    private final DeliveryService deliveryService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public DeliveryController(DeliveryService deliveryService,
                              CircuitBreakerFactory circuitBreakerFactory) {
        this.deliveryService = deliveryService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @PutMapping
    @Transactional
    public ResponseEntity<DeliveryDto> createDelivery(@RequestBody CreateNewDeliveryRequest request) {
        log.info("Creating new delivery for order request");

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("deliveryService");

        DeliveryDto deliveryDto = circuitBreaker.run(() -> {
            return deliveryService.createDelivery(request);
        }, throwable -> {
            log.error("Circuit breaker triggered for createDelivery - Error: {}", throwable.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service Unavailable: Unable to create delivery.",
                    throwable
            );
        });

        return ResponseEntity.ok(deliveryDto);
    }

    @PostMapping("/successful")
    @Transactional
    public ResponseEntity<Void> successfulDelivery(@RequestBody UUID orderId) {
        log.info("Marking delivery as successful: orderId={}", orderId);

        deliveryService.successfulDelivery(orderId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/picked")
    @Transactional
    public ResponseEntity<Void> pickProducts(@RequestBody UUID orderId) {
        log.info("Products picked up for order: orderId={}", orderId);

        deliveryService.pickProducts(orderId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/failed")
    @Transactional
    public ResponseEntity<Void> failedDelivery(@RequestBody UUID orderId) {
        log.info("Marking delivery as failed: orderId={}", orderId);

        deliveryService.failedDelivery(orderId);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/cost")
    public ResponseEntity<BigDecimal> calculateDeliveryCost(@RequestBody UUID deliveryId) {
        log.info("Calculating delivery cost for deliveryId: {}", deliveryId);

        BigDecimal cost = deliveryService.calculateDeliveryCost(deliveryId);

        return ResponseEntity.ok(cost);
    }
}