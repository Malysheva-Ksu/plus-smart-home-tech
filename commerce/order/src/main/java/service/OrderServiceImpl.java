package service;

import client.DeliveryClient;
import client.PaymentClient;
import client.WarehouseServiceClient;
import exception.NoOrderFoundException;
import exception.NotAuthorizedUserException;
import jakarta.transaction.Transactional;
import mapper.OrderMapper;
import model.delivery.BookedProductsDto;
import model.delivery.CreateNewDeliveryRequest;
import model.delivery.DeliveryDto;
import model.order.*;
import model.payment.PaymentDto;
import model.warehouse.AddressDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final WarehouseServiceClient warehouseClient;
    private final PaymentClient paymentClient;
    private final DeliveryClient deliveryClient;

    public OrderServiceImpl(OrderRepository orderRepository,
                            WarehouseServiceClient warehouseClient,
                            PaymentClient paymentClient,
                            DeliveryClient deliveryClient) {
        this.orderRepository = orderRepository;
        this.warehouseClient = warehouseClient;
        this.paymentClient = paymentClient;
        this.deliveryClient = deliveryClient;
    }

    @Override
    public List<OrderDto> getUserOrders(String username) {
        log.info("Fetching all orders for user: {}", username);

        checkUsername(username);
        List<Order> orders = orderRepository.findByUsername(username);

        return OrderMapper.toOrderDtoList(orders);
    }

    @Override
    @Transactional
    public OrderDto createNewOrder(String username, CreateNewOrderRequest request) {
        log.info("Starting complex process of creating new order for user: {}", username);

        checkUsername(username);

        Order order = initializeOrder(request, username);
        reserveProductsAndSetDeliveryDetails(order, request);
        fetchAndSetPrices(order);
        setOnDelivery(order);

        log.info("Order process completed successfully for user: {}, orderId: {}", username, order.getOrderId());
        return saveAndMap(order);
    }

    @Override
    @Transactional
    public OrderDto returnProducts(ProductReturnRequest request) {
        log.info("Processing product return for orderId: {}", request.orderId());

        Order order = getOrderById(request.orderId());
        Map<UUID, Integer> productsToReturn = request.products();

        returnBookedProducts(productsToReturn);
        updateProductQuantities(order, productsToReturn);

        order.setOrderState(OrderState.PRODUCT_RETURNED);

        log.info("Products returned and order state updated for orderId: {}", request.orderId());
        return saveAndMap(order);
    }

    @Override
    @Transactional
    public OrderDto payOrder(UUID orderId) {
        log.info("Initiating payment process for orderId: {}", orderId);

        Order order = getOrderById(orderId);
        PaymentDto payment = createPayment(order);

        order.setPaymentId(payment.getPaymentId());
        order.setOrderState(OrderState.ON_PAYMENT);

        return saveAndMap(order);
    }

    @Override
    @Transactional
    public OrderDto successfulPayment(UUID orderId) {
        log.info("Payment confirmed for orderId: {}", orderId);
        return changeOrderState(orderId, OrderState.PAID);
    }

    @Override
    @Transactional
    public OrderDto paymentFailed(UUID orderId) {
        log.warn("Payment failed for orderId: {}", orderId);
        return changeOrderState(orderId, OrderState.PAYMENT_FAILED);
    }

    @Override
    @Transactional
    public OrderDto successfulDelivery(UUID orderId) {
        log.info("Delivery successful for orderId: {}", orderId);
        return changeOrderState(orderId, OrderState.DELIVERED);
    }

    @Override
    @Transactional
    public OrderDto deliveryFailed(UUID orderId) {
        log.error("Delivery failed report for orderId: {}", orderId);
        return changeOrderState(orderId, OrderState.DELIVERY_FAILED);
    }

    @Override
    @Transactional
    public OrderDto completeOrder(UUID orderId) {
        log.info("Finalizing order: {}", orderId);
        return changeOrderState(orderId, OrderState.COMPLETED);
    }

    @Override
    @Transactional
    public OrderDto calculateTotal(UUID orderId) {
        log.info("Recalculating total price for orderId: {}", orderId);

        Order order = getOrderById(orderId);
        BigDecimal totalCost = getTotalPrice(order);
        order.setTotalPrice(totalCost);

        return saveAndMap(order);
    }

    @Override
    @Transactional
    public OrderDto calculateDelivery(UUID orderId) {
        log.info("Calculating delivery cost for orderId: {}", orderId);

        Order order = getOrderById(orderId);
        BigDecimal deliveryPrice = getDeliveryPrice(order);

        order.getDeliveryDetails().setDeliveryPrice(deliveryPrice);

        return saveAndMap(order);
    }

    @Override
    @Transactional
    public OrderDto assembleOrder(UUID orderId) {
        log.info("Starting order assembly at warehouse: orderId={}", orderId);

        Order order = getOrderById(orderId);
        assembleProducts(order);
        order.setOrderState(OrderState.ASSEMBLED);

        return saveAndMap(order);
    }

    @Override
    @Transactional
    public OrderDto assemblyFailed(UUID orderId) {
        log.warn("Assembly failed for orderId: {}", orderId);
        return changeOrderState(orderId, OrderState.ASSEMBLY_FAILED);
    }


    private Order initializeOrder(CreateNewOrderRequest request, String username) {
        Order order = OrderMapper.toOrder(request, username);
        order.setOrderId(UUID.randomUUID());
        order.setOrderState(OrderState.NEW);

        return order;
    }

    private void reserveProductsAndSetDeliveryDetails(Order order, CreateNewOrderRequest request) {
        log.debug("Reserving products in warehouse for order: {}", order.getOrderId());
        BookedProductsDto booked = warehouseClient.checkProductsQuantity(request.shoppingCart());
        createDeliveryDetails(booked, order);
    }

    private void fetchAndSetPrices(Order order) {
        BigDecimal productPrice = paymentClient.getProductCost(OrderMapper.toOrderDto(order));
        order.setProductPrice(productPrice);
    }

    private OrderDto saveAndMap(Order order) {
        Order savedOrder = orderRepository.save(order);
        return OrderMapper.toOrderDto(savedOrder);
    }

    private OrderDto changeOrderState(UUID orderId, OrderState state) {
        Order order = getOrderById(orderId);
        order.setOrderState(state);

        return saveAndMap(order);
    }

    private void checkUsername(String username) {
        if (username == null || username.isBlank()) {
            log.error("Authorization check failed: username is null or blank");
            throw new NotAuthorizedUserException("User not authorized");
        }
    }

    private Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> {
                    log.error("Order with id {} not found", orderId);
                    return new NoOrderFoundException("Order not found");
                });
    }

    private void updateProductQuantities(Order order, Map<UUID, Integer> returnedProducts) {
        log.debug("Updating product quantities in order after return");

        order.getProducts().forEach(product -> {
            Integer returnQuantity = returnedProducts.get(product.getProductId());
            if (returnQuantity != null) {int newQuantity = Math.max(0, product.getQuantity() - returnQuantity);
                product.setQuantity(newQuantity);
            }
        });
    }

    private void setOnDelivery(Order order) {
        DeliveryDto deliveryDto = createDelivery(order);
        order.getDeliveryDetails().setDeliveryId(deliveryDto.getDeliveryId());
        order.setOrderState(OrderState.ON_DELIVERY);
    }

    private DeliveryDto createDelivery(Order order) {
        AddressDto fromAddress = warehouseClient.getWarehouseAddress();
        AddressDto toAddress = OrderMapper.toAddressDto(order.getDeliveryAddress());

        CreateNewDeliveryRequest request = new CreateNewDeliveryRequest(
                OrderMapper.toOrderDto(order),
                fromAddress,
                toAddress
        );

        return deliveryClient.createDelivery(request);
    }

    private void returnBookedProducts(Map<UUID, Integer> products) {
        warehouseClient.returnBookedProducts(products);
    }

    private PaymentDto createPayment(Order order) {
        return paymentClient.createPayment(OrderMapper.toOrderDto(order));
    }

    private BigDecimal getDeliveryPrice(Order order) {
        return deliveryClient.calculateDeliveryCost(order.getDeliveryDetails().getDeliveryId());
    }

    private BigDecimal getTotalPrice(Order order) {
        return paymentClient.getTotalCost(OrderMapper.toOrderDto(order));
    }

    private void createDeliveryDetails(BookedProductsDto bookedProducts, Order order) {
        DeliveryDetails deliveryDetails = new DeliveryDetails();

        deliveryDetails.setDeliveryVolume(bookedProducts.getDeliveryVolume());
        deliveryDetails.setDeliveryWeight(bookedProducts.getDeliveryWeight());
        deliveryDetails.setFragile(bookedProducts.isFragile());

        order.setDeliveryDetails(deliveryDetails);
    }

    private void assembleProducts(Order order) {
        AssemblyProductsForOrderRequest request = new AssemblyProductsForOrderRequest(
                OrderMapper.toOrderProductDtoMap(order.getProducts()),
                order.getOrderId()
        );

        warehouseClient.assemblyProducts(request);
    }
}