package service;

import model.delivery.CreateNewDeliveryRequest;
import model.delivery.DeliveryDto;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryService {

    DeliveryDto createDelivery(CreateNewDeliveryRequest request);

    void successfulDelivery(UUID orderId);

    void pickProducts(UUID orderId);

    void failedDelivery(UUID orderId);

    BigDecimal calculateDeliveryCost(UUID deliveryId);

}