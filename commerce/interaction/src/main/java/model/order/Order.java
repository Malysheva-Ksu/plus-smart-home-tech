package model.order;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "orders")
public class Order {
    @Id
    @Column(name = "order_id")
    private UUID orderId;
    private String username;

    @Column(name = "state")
    @Enumerated(EnumType.STRING)
    private OrderState orderState = OrderState.NEW;

    @Column(name = "shopping_cart_id")
    private UUID shoppingCartId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id")
    private List<OrderProduct> products;

    @Column(name = "payment_id")
    private UUID paymentId;

    @Embedded
    private DeliveryDetails deliveryDetails;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "delivery_address_id", referencedColumnName = "id")
    private Address deliveryAddress;

    @Column(name = "total_price")
    private BigDecimal totalPrice;

    @Column(name = "product_price")
    private BigDecimal productPrice;
}