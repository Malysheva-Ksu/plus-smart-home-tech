package controller;


import client.PaymentClient;
import jakarta.transaction.Transactional;
import model.order.OrderDto;
import model.payment.PaymentDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import service.PaymentService;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/payment")
public class PaymentController {

    private static final Logger log = LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public PaymentController(PaymentService paymentService,
                             CircuitBreakerFactory circuitBreakerFactory) {
        this.paymentService = paymentService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @PostMapping
    @Transactional
    public ResponseEntity<PaymentDto> createPayment(@RequestBody OrderDto order) {
        log.info("Initiating payment creation for orderId: {}", order.getOrderId());

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("paymentService");

        PaymentDto paymentDto = circuitBreaker.run(() -> {
            return paymentService.createPayment(order);
        }, throwable -> {
            log.error("Circuit breaker triggered for createPayment - OrderId: {}, Error: {}",
                    order.getOrderId(), throwable.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment service is currently unavailable.",
                    throwable
            );
        });

        return ResponseEntity.status(HttpStatus.CREATED).body(paymentDto);
    }

    @PostMapping("/totalCost")
    public ResponseEntity<BigDecimal> getTotalCost(@RequestBody OrderDto order) {
        log.info("Calculating total cost for order: {}", order.getOrderId());

        BigDecimal totalCost = paymentService.getTotalCost(order);
        return ResponseEntity.ok(totalCost);
    }

    @PostMapping("/refund")
    @Transactional
    public ResponseEntity<Void> successfulPayment(@RequestBody UUID paymentId) {
        log.info("Processing successful payment/refund for paymentId: {}", paymentId);

        paymentService.successfulPayment(paymentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/productCost")
    public ResponseEntity<BigDecimal> getProductCost(@RequestBody OrderDto order) {
        log.info("Getting product cost for order: {}", order.getOrderId());

        BigDecimal productCost = paymentService.getProductCost(order);
        return ResponseEntity.ok(productCost);
    }

    @PostMapping("/failed")
    @Transactional
    public ResponseEntity<Void> failedPayment(@RequestBody UUID paymentId) {
        log.warn("Recording failed payment for paymentId: {}", paymentId);

        paymentService.failedPayment(paymentId);
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}