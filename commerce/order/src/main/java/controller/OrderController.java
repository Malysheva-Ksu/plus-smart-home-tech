package controller;

import client.OrderClient;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import model.order.CreateNewOrderRequest;
import model.order.OrderDto;
import model.order.ProductReturnRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import service.OrderService;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/order")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    private final OrderService orderService;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public OrderController(OrderService orderService,
                           CircuitBreakerFactory circuitBreakerFactory) {
        this.orderService = orderService;
        this.circuitBreakerFactory = circuitBreakerFactory;
    }

    @GetMapping
    public ResponseEntity<List<OrderDto>> getUserOrders(@RequestParam String username) {
        log.info("Fetching orders for user: {}", username);

        List<OrderDto> orders = orderService.getUserOrders(username);
        return ResponseEntity.ok(orders);
    }

    @PutMapping
    @Transactional
    public ResponseEntity<OrderDto> createNewOrder(
            @RequestParam String username,
            @RequestBody @Valid CreateNewOrderRequest request) {

        log.info("Starting order creation for user: {}", username);

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("orderService");

        OrderDto orderDto = circuitBreaker.run(() -> {
            return orderService.createNewOrder(username, request);
        }, throwable -> {
            log.error("Circuit breaker triggered for createNewOrder - user: {}, error: {}",
                    username, throwable.getMessage());
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Service Unavailable: Unable to create order at this time.",
                    throwable
            );
        });

        log.info("Order successfully created for user: {}", username);
        return ResponseEntity.status(HttpStatus.CREATED).body(orderDto);
    }

    @PostMapping("/return")
    @Transactional
    public ResponseEntity<OrderDto> returnProducts(@RequestBody @Valid ProductReturnRequest request) {
        log.info("Processing product return for orderId: {}", request.orderId());

        OrderDto response = orderService.returnProducts(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment")
    @Transactional
    public ResponseEntity<OrderDto> payOrder(@RequestBody UUID orderId) {
        log.info("Initiating payment for orderId: {}", orderId);

        OrderDto response = orderService.payOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment/failed")
    @Transactional
    public ResponseEntity<OrderDto> paymentFailed(@RequestBody UUID orderId) {
        log.warn("Payment failed for orderId: {}", orderId);

        OrderDto response = orderService.paymentFailed(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/payment/success")
    @Transactional
    public ResponseEntity<OrderDto> successfulPayment(@RequestBody UUID orderId) {
        log.info("Payment successful for orderId: {}", orderId);

        OrderDto response = orderService.successfulPayment(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delivery/success")
    @Transactional
    public ResponseEntity<OrderDto> successfulDelivery(@RequestBody UUID orderId) {
        log.info("Delivery confirmed for orderId: {}", orderId);

        OrderDto response = orderService.successfulDelivery(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/delivery/failed")
    @Transactional
    public ResponseEntity<OrderDto> deliveryFailed(@RequestBody UUID orderId) {
        log.error("Delivery failed for orderId: {}", orderId);

        OrderDto response = orderService.deliveryFailed(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/completed")
    @Transactional
    public ResponseEntity<OrderDto> completeOrder(@RequestBody UUID orderId) {
        log.info("Completing orderId: {}", orderId);

        OrderDto response = orderService.completeOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate/total")
    public ResponseEntity<OrderDto> calculateTotal(@RequestBody UUID orderId) {
        log.info("Calculating total for orderId: {}", orderId);

        OrderDto response = orderService.calculateTotal(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/calculate/delivery")
    public ResponseEntity<OrderDto> calculateDelivery(@RequestBody UUID orderId) {
        log.info("Calculating delivery cost for orderId: {}", orderId);

        OrderDto response = orderService.calculateDelivery(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assembly")
    @Transactional
    public ResponseEntity<OrderDto> assembleOrder(@RequestBody UUID orderId) {
        log.info("Starting assembly for orderId: {}", orderId);

        OrderDto response = orderService.assembleOrder(orderId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/assembly/failed")
    @Transactional
    public ResponseEntity<OrderDto> assemblyFailed(@RequestBody UUID orderId) {
        log.error("Assembly failed for orderId: {}", orderId);

        OrderDto response = orderService.assemblyFailed(orderId);
        return ResponseEntity.ok(response);
    }
}