package service;

import client.OrderClient;
import client.ProductServiceClient;
import exception.NoOrderFoundException;
import exception.NotEnoughInfoInOrderToCalculateException;
import jakarta.transaction.Transactional;
import mapper.PaymentMapper;
import model.order.OrderDto;
import model.payment.Payment;
import model.payment.PaymentDto;
import model.payment.PaymentState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository repository;
    private final ProductServiceClient shoppingStoreClient;
    private final OrderClient orderClient;

    public PaymentServiceImpl(PaymentRepository repository,
                              ProductServiceClient shoppingStoreClient,
                              OrderClient orderClient) {
        this.repository = repository;
        this.shoppingStoreClient = shoppingStoreClient;
        this.orderClient = orderClient;
    }

    @Override
    @Transactional
    public PaymentDto createPayment(OrderDto order) {
        log.info("Creating payment for orderId: {}", order.getOrderId());

        Payment payment = PaymentMapper.toPayment(order);
        validatePrices(payment.getProductPrice(), payment.getDeliveryPrice());

        BigDecimal total = payment.getTotalPrice();

        if (Objects.isNull(total)) {
            log.error("Payment creation failed: total cost is missing for orderId {}", order.getOrderId());
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Not enough information in order to calculate: no information about total cost"
            );
        }

        payment.setPaymentId(UUID.randomUUID());
        Payment savedPayment = repository.save(payment);

        BigDecimal feeTotal = calculateFee(payment.getProductPrice());

        log.info("Payment record created with id: {} for order: {}", savedPayment.getPaymentId(), order.getOrderId());
        return PaymentMapper.toPaymentDto(savedPayment, feeTotal);
    }

    @Override
    public BigDecimal getTotalCost(OrderDto order) {
        log.info("Calculating total cost (including fee) for order: {}", order.getOrderId());

        BigDecimal deliveryPrice = order.getDeliveryPrice();
        BigDecimal productPrice = order.getProductPrice();

        validatePrices(productPrice, deliveryPrice);

        BigDecimal fee = calculateFee(productPrice);
        BigDecimal totalCost = deliveryPrice.add(productPrice).add(fee);

        log.info("Calculated total cost for order {}: {}", order.getOrderId(), totalCost);
        return totalCost;
    }

    @Override
    @Transactional
    public void successfulPayment(UUID paymentId) {
        log.info("Marking payment as successful: paymentId={}", paymentId);

        Payment payment = getPaymentById(paymentId);
        payment.setPaymentState(PaymentState.SUCCESS);

        repository.save(payment);
        orderClient.successfulPayment(payment.getOrderId());

        log.info("Payment {} successfully processed for order {}", paymentId, payment.getOrderId());
    }

    @Override
    @Transactional
    public void failedPayment(UUID paymentId) {
        log.warn("Marking payment as failed: paymentId={}", paymentId);

        Payment payment = getPaymentById(paymentId);
        payment.setPaymentState(PaymentState.FAILED);

        repository.save(payment);
        orderClient.paymentFailed(payment.getOrderId());

        log.info("Payment {} marked as FAILED for order {}", paymentId, payment.getOrderId());
    }

    @Override
    public BigDecimal getProductCost(OrderDto order) {
        log.info("Calculating products cost from store for order: {}", order.getOrderId());

        Map<UUID, Integer> products = order.getProducts();
        List<UUID> productsIds = products.keySet().stream().collect(Collectors.toList());

        Map<UUID, BigDecimal> priceById = shoppingStoreClient.getProductsPrice(productsIds);

        BigDecimal productsCost = priceById.entrySet().stream()
                .map(entry -> {
                    int quantity = products.getOrDefault(entry.getKey(), 0);
                    return entry.getValue().multiply(BigDecimal.valueOf(quantity));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.info("Total products cost for order {}: {}", order.getOrderId(), productsCost);
        return productsCost;
    }

    private BigDecimal calculateFee(BigDecimal productPrice) {
        return productPrice.multiply(BigDecimal.valueOf(0.10));
    }

    private Payment getPaymentById(UUID paymentId) {
        return repository.findById(paymentId)
                .orElseThrow(() -> {
                    log.error("Payment not found for id: {}", paymentId);
                    return new NoOrderFoundException("Payment was not found");
                });
    }

    private void validatePrices(BigDecimal productPrice, BigDecimal deliveryPrice) {
        if (Objects.isNull(deliveryPrice)) {
            log.error("Validation failed: delivery price is null");
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Not enough information in order to calculate: no information about delivery"
            );
        }

        if (Objects.isNull(productPrice)) {
            log.error("Validation failed: product price is null");
            throw new NotEnoughInfoInOrderToCalculateException(
                    "Not enough information in order to calculate: no information about products price"
            );
        }
    }
}