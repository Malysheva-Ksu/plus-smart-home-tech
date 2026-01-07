package service;


import model.order.OrderDto;
import model.payment.PaymentDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface PaymentService {
    PaymentDto createPayment(OrderDto order);

    BigDecimal getTotalCost(OrderDto order);

    void successfulPayment(UUID paymentId);

    BigDecimal getProductCost(OrderDto order);

    void failedPayment(UUID paymentId);
}