package model.order;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

@Embeddable
@Getter
@Setter
@ToString
@NoArgsConstructor
public class DeliveryDetails {
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Column(name = "delivery_weight")
    private double deliveryWeight;

    @Column(name = "delivery_volume")
    private double deliveryVolume;

    @Column(name = "delivery_fragile")
    private boolean fragile;

    @Column(name = "delivery_price")
    private BigDecimal deliveryPrice;
}