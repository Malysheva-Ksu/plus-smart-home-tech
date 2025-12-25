package model.delivery;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "model/delivery")
public class Delivery {
    @Id
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "delivery_volume")
    private double deliveryVolume;

    @Column(name = "delivery_weight")
    private double deliveryWeight;
    private boolean fragile;

    @JoinColumn(name = "address_from_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL)
    private Address fromAddress;

    @JoinColumn(name = "address_to_id", referencedColumnName = "id")
    @OneToOne(cascade = CascadeType.ALL)
    private Address toAddress;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "delivery_state")
    private DeliveryState deliveryState = DeliveryState.CREATED;
}