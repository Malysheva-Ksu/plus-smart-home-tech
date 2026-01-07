package service;

import client.OrderClient;
import client.WarehouseServiceClient;
import exception.NoDeliveryFoundException;
import model.delivery.*;
import jakarta.transaction.Transactional;
import mapper.DeliveryMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import repository.DeliveryRepository;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class DeliveryServiceImpl implements DeliveryService {

    private static final Logger log = LoggerFactory.getLogger(DeliveryServiceImpl.class);

    private static final String WAREHOUSE_ADDRESS_1 = "ADDRESS_1";
    private static final String WAREHOUSE_ADDRESS_2 = "ADDRESS_2";

    private final DeliveryRepository repository;
    private final OrderClient orderClient;
    private final WarehouseServiceClient warehouseClient;

    public DeliveryServiceImpl(DeliveryRepository repository,
                               OrderClient orderClient,
                               WarehouseServiceClient warehouseClient) {
        this.repository = repository;
        this.orderClient = orderClient;
        this.warehouseClient = warehouseClient;
    }

    @Override
    @Transactional
    public DeliveryDto createDelivery(CreateNewDeliveryRequest request) {
        log.info("Starting creation of new delivery");

        Delivery delivery = DeliveryMapper.toDelivery(request);
        delivery.setDeliveryId(UUID.randomUUID());

        Delivery savedDelivery = repository.save(delivery);

        log.info("Delivery successfully created with id: {}", savedDelivery.getDeliveryId());
        return DeliveryMapper.toDeliveryDto(savedDelivery);
    }

    @Override
    @Transactional
    public void successfulDelivery(UUID orderId) {
        log.info("Processing successful delivery for orderId: {}", orderId);

        Delivery delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.DELIVERED);

        repository.save(delivery);
        orderClient.successfulDelivery(orderId);

        log.info("Delivery status updated to DELIVERED for orderId: {}", orderId);
    }

    @Override
    @Transactional
    public void pickProducts(UUID orderId) {
        log.info("Picking products for delivery: orderId={}", orderId);

        Delivery delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.IN_PROGRESS);

        ShippedToDeliveryRequest request = new ShippedToDeliveryRequest(orderId, delivery.getDeliveryId());

        warehouseClient.shipProducts(request);
        repository.save(delivery);

        log.info("Products shipped and delivery status updated to IN_PROGRESS: orderId={}", orderId);
    }

    @Override
    @Transactional
    public void failedDelivery(UUID orderId) {
        log.error("Handling failed delivery for orderId: {}", orderId);

        Delivery delivery = getByOrderId(orderId);
        delivery.setDeliveryState(DeliveryState.FAILED);

        repository.save(delivery);
        orderClient.deliveryFailed(orderId);

        log.info("Delivery status updated to FAILED for orderId: {}", orderId);
    }

    @Override
    public BigDecimal calculateDeliveryCost(UUID deliveryId) {
        log.info("Calculating delivery cost for deliveryId={}", deliveryId);

        Delivery delivery = repository.findById(deliveryId)
                .orElseThrow(() -> {
                    log.warn("Delivery calculation failed: deliveryId {} not found", deliveryId);
                    return new NoDeliveryFoundException("Delivery not found");
                });

        BigDecimal price = BigDecimal.valueOf(5.0);
        String fromStreet = delivery.getFromAddress().getStreet();
        String toStreet = delivery.getToAddress().getStreet();

        if (fromStreet.contains(WAREHOUSE_ADDRESS_2)) {
            price = price.add(price.multiply(BigDecimal.valueOf(2)));
        }

        if (fromStreet.contains(WAREHOUSE_ADDRESS_1)) {
            price = price.add(price);
        }

        if (delivery.isFragile()) {
            price = price.add(price.multiply(BigDecimal.valueOf(0.2)));
        }

        price = price.add(BigDecimal.valueOf(delivery.getDeliveryWeight()).multiply(BigDecimal.valueOf(0.3)));
        price = price.add(BigDecimal.valueOf(delivery.getDeliveryVolume()).multiply(BigDecimal.valueOf(0.2)));

        if (!fromStreet.equals(toStreet)) {
            price = price.add(price.multiply(BigDecimal.valueOf(0.2)));
        }

        log.info("Final calculated cost for deliveryId {}: {}", deliveryId, price);
        return price;
    }

    private Delivery getByOrderId(UUID orderId) {
        return repository.findByOrderId(orderId)
                .orElseThrow(() -> {
                    log.error("Could not find delivery for orderId: {}", orderId);
                    return new NoDeliveryFoundException("Delivery not found");
                });
    }
}