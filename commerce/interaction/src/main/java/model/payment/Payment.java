package model.payment;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "payment")
public class Payment {
    @Id
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "product_price")
    private BigDecimal productPrice;

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_state")
    private PaymentState paymentState;

    @Column(name = "order_id")
    private UUID orderId;
}