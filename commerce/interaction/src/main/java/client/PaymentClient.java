package client;

import model.order.OrderDto;
import model.payment.PaymentDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient {
    @PostMapping
    PaymentDto createPayment(@RequestBody OrderDto order);

    @PostMapping("/totalCost")
    BigDecimal getTotalCost(@RequestBody OrderDto order);

    @PostMapping("/refund")
    void successfulPayment(@RequestBody UUID paymentId);

    @PostMapping("/productCost")
    BigDecimal getProductCost(@RequestBody OrderDto order);

    @PostMapping("/failed")
    void failedPayment(@RequestBody UUID paymentId);
}