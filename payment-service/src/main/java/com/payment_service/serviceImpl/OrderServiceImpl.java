package com.payment_service.serviceImpl;

import com.payment_service.client.CustomerServiceClient;
import com.payment_service.dto.request.CreateOrderRequest;
import com.payment_service.dto.response.OrderResponse;
import com.payment_service.dto.response.PaymentResponse;
import com.payment_service.entity.OrderRecord;
import com.payment_service.mapper.OrderMapper;
import com.payment_service.mapper.PaymentMapper;
import com.payment_service.outbox.OutboxEventPublisher;
import com.payment_service.repository.OrderRepository;
import com.payment_service.repository.PaymentRepository;
import com.payment_service.service.OrderService;
import common_lib.dto.FindOrCreateCustomerRequest;
import common_lib.enums.EventAggregateType;
import common_lib.enums.OrderStatus;
import common_lib.exceptions.BusinessRuleViolationException;
import common_lib.exceptions.DuplicateResourceException;
import common_lib.exceptions.ResourceNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) /* For GET queries, readOnly=true will not lock the connection */
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderMapper orderMapper;
    private final OutboxEventPublisher eventPublisher;
    private final CustomerServiceClient customerServiceClient;

    @Value("${payment.order.default-order-expiry-seconds:30}")
    private int defaultOrderExpirySeconds;

    /**
     * Rejects a duplicate receipt within the merchant, resolves an optional customer, applies the default expiry when
     * absent, persists the order, and records {@code ORDER_CREATED} in the outbox.
     *
     * @throws DuplicateResourceException if the merchant already used the receipt
     */
    @Override
    @Transactional
    @CircuitBreaker(name = "merchant-service")
    @Retry(name = "merchant-service")   //For retry, operation must be idempotent
    public OrderResponse create(UUID merchantId, CreateOrderRequest createOrderRequest) {
        if (createOrderRequest.receipt() != null &&
                orderRepository.existsByReceiptAndMerchant(createOrderRequest.receipt(), merchantId))
            throw new DuplicateResourceException("ORDER_RECEIPT_DUPLICATE", "Order already exists");

        UUID customerId = null;
        CreateOrderRequest.CustomerDetails customer = createOrderRequest.customer();
        if (customer != null) {
            // Meaning, the order request is from an active customer account, not a guest one.
            customerId = customerServiceClient.findOrCreate(new FindOrCreateCustomerRequest(merchantId,
                    customer.name(), customer.phone(), customer.email()));
        }

        OrderRecord orderRecord = OrderRecord.builder()
                .notes(createOrderRequest.notes())
                .amount(createOrderRequest.amount())
                .receipt(createOrderRequest.receipt())
                .merchant(merchantId)
                .customerId(customerId)
                .expiresAt(createOrderRequest.expiresAt() != null ? createOrderRequest.expiresAt()
                        : Instant.now().plusSeconds(defaultOrderExpirySeconds))
                .build();
        log.info("Order Record: {}", orderRecord);
        orderRecord = orderRepository.save(orderRecord);
        log.info("Order Record: {}", orderRecord);
        eventPublisher.publish(EventAggregateType.ORDER, orderRecord.getId(), "ORDER_CREATED",
                Map.of("orderId", orderRecord.getId(),
                        "merchantId", merchantId.toString(),
                        "status", orderRecord.getStatus().name(),
                        "amountUnits", orderRecord.getAmount().getAmountUnits(),
                        "amountCurrency", orderRecord.getAmount().getCurrency()));
        return orderMapper.toResponse(orderRecord);
    }

    /**
     * Loads and maps an order only when both its ID and merchant ID match; foreign IDs are indistinguishable from missing.
     */
    @Override
    public OrderResponse get(UUID merchantId, UUID orderId) {
        return orderRepository.findByIdAndMerchant(orderId, merchantId)
                .map(orderRecord -> orderMapper.toResponse(orderRecord))
                .orElseThrow(() -> new ResourceNotFoundException("order", orderId));
    }

    /**
     * Marks a merchant-owned order cancelled and records {@code ORDER_CANCELLED}; paid or already-cancelled orders are
     * rejected as a business-rule violation, and missing or foreign orders fail as not found.
     */
    @Override
    @Transactional
    public String cancel(UUID merchantId, UUID orderId) {
        return orderRepository.findByIdAndMerchant(orderId, merchantId)
                .map(orderRecord -> {
                    if (orderRecord.getStatus().equals(OrderStatus.CANCELLED)
                            || orderRecord.getStatus().equals(OrderStatus.PAID))
                        throw new BusinessRuleViolationException("ORDER_CANNOT_CANCEL",
                                "Order is already cancelled/paid", "ORDER", orderId);
                    orderRecord.setStatus(OrderStatus.CANCELLED);
                    orderRecord.setUpdatedAt(Instant.now());
                    orderRepository.save(orderRecord);

                    eventPublisher.publish(EventAggregateType.ORDER, orderRecord.getId(), "ORDER_CANCELLED",
                            Map.of("orderId", orderRecord.getId(),
                                    "merchantId", merchantId.toString(),
                                    "status", orderRecord.getStatus().name(),
                                    "amountUnits", orderRecord.getAmount().getAmountUnits(),
                                    "amountCurrency", orderRecord.getAmount().getCurrency()));

                    return "Order with orderId : " + orderId + " Cancelled Successfully";
                })
                .orElseThrow(() -> new ResourceNotFoundException("order", orderId));
    }

    /**
     * Verifies merchant ownership before querying and mapping all payment attempts for the order.
     */
    @Override
    public List<PaymentResponse> list(UUID merchantId, UUID orderId) {
        if (!orderRepository.existsByIdAndMerchant(orderId, merchantId))
            throw new ResourceNotFoundException("order", orderId);
        return paymentMapper.toResponseList(paymentRepository.findAllByOrderRecordId(orderId));
    }
}
