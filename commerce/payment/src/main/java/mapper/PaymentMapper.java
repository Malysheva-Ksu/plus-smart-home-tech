package mapper;

import model.order.OrderDto;
import model.payment.Payment;
import model.payment.PaymentDto;
import model.payment.PaymentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;

public final class PaymentMapper {

    private static final Logger log = LoggerFactory.getLogger(PaymentMapper.class);

    private PaymentMapper() {
    }

    public static Payment toPayment(OrderDto order) {
        log.debug("Mapping OrderDto to Payment entity for orderId: {}", order.getOrderId());

        Payment payment = new Payment();

        payment.setPaymentState(PaymentState.PENDING);
        payment.setDeliveryPrice(order.getDeliveryPrice());
        payment.setProductPrice(order.getProductPrice());
        payment.setTotalPrice(order.getTotalPrice());
        payment.setOrderId(order.getOrderId());

        return payment;
    }

    public static PaymentDto toPaymentDto(Payment payment, BigDecimal feeTotal) {
        log.debug("Mapping Payment entity to PaymentDto for paymentId: {}", payment.getPaymentId());

        PaymentDto dto = new PaymentDto();

        dto.setPaymentId(payment.getPaymentId());
        dto.setTotalPayment(payment.getTotalPrice());
        dto.setDeliveryTotal(payment.getDeliveryPrice());
        dto.setProductTotal(payment.getProductPrice());
        dto.setFeeTotal(feeTotal);

        return dto;
    }
}