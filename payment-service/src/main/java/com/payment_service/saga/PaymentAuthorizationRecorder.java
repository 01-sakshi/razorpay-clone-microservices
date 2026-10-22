package com.payment_service.saga;

import com.payment_service.dto.request.PaymentInitRequest;
import com.payment_service.dto.response.PaymentResponse;
import com.payment_service.entity.OrderRecord;
import com.payment_service.entity.Payment;
import com.payment_service.gateway.dto.PaymentResult;
import com.payment_service.mapper.PaymentMapper;
import com.payment_service.outbox.OutboxEventPublisher;
import com.payment_service.repository.OrderRepository;
import com.payment_service.repository.PaymentRepository;
import com.payment_service.statemachine.PaymentTransitionService;
import common_lib.enums.EventAggregateType;
import common_lib.enums.OrderStatus;
import common_lib.enums.PaymentEvent;
import common_lib.enums.PaymentStatus;
import common_lib.exceptions.BusinessRuleViolationException;
import common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentAuthorizationRecorder {

    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentTransitionService paymentTransitionService;
    private final OutboxEventPublisher eventPublisher;
    private final PaymentMapper paymentMapper;

    @Transactional
    public Payment recordPayment(UUID merchantId, PaymentInitRequest request) {
        // OrderRecord orderRecord =
        // orderRepository.findByIdAndMerchant(request.orderId(), merchantId)
        // .orElseThrow(() -> new ResourceNotFoundException("order",
        // request.orderId()));

        // To handle scenarios respect to race conditions on a single resource
        // Pessimistic Locking: It will only block the transactions having same order
        // and merchant ID
        OrderRecord orderRecord = orderRepository.findByIdAndMerchantForUpdate(request.orderId(), merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("order", request.orderId()));

        if (!orderRecord.getStatus().equals(OrderStatus.CREATED) &&
                !orderRecord.getStatus().equals(OrderStatus.ATTEMPTED)) {
            log.warn("Order is not in a payable state, orderId: {}, status: {}", request.orderId(),
                    orderRecord.getStatus());
            throw new BusinessRuleViolationException("ORDER_NOT_PAYABLE", "Order is not in a payable state", "ORDER",
                    request.orderId());
        }

        orderRecord.setStatus(OrderStatus.ATTEMPTED);
        orderRecord.setAttempts(orderRecord.getAttempts() + 1); // payment attempt on this order

        Payment payment = Payment
                .builder()
                .orderRecord(orderRecord)
                .status(PaymentStatus.CREATED)
                .merchantId(merchantId)
                .amount(orderRecord.getAmount())
                .method(request.paymentMethod())
                .methodDetails(request.methodDetails())
                .build();
        payment = paymentRepository.save(payment);
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_ATTEMPT);
        return payment;
    }

    @Transactional
    public PaymentResponse compensateAuthorizationFailure(UUID paymentId, String errorCode, String errorDescription) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
                new ResourceNotFoundException("PAYMENT", paymentId));
        paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
        payment.setErrorCode(errorCode);
        payment.setErrorDescription(errorDescription);
        payment = paymentRepository.save(payment);
        publishStatusEvent(payment, "PAYMENT_AUTHORIZATION_COMPENSATED");
        return paymentMapper.toResponse(payment);
    }

    public void publishStatusEvent(Payment payment, String eventType) {
        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), eventType,
                Map.of("orderId", payment.getOrderRecord().getId(),
                        "paymentId", payment.getId(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getOrderRecord().getAmount().getAmountUnits(),
                        "amountCurrency", payment.getOrderRecord().getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()));
    }

    @Transactional
    public PaymentResponse applyGatewayResult(UUID paymentId, PaymentResult paymentResult) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
                new ResourceNotFoundException("PAYMENT", paymentId));
        switch (paymentResult) {
            case PaymentResult.Pending pending -> // record pattern
                    payment.setProcessorReference(pending.registrationRef());
            case PaymentResult.Failure failure -> {
                IO.println("failure:" + failure);
                // payment.setStatus(PaymentStatus.FAILED);
                paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
            case PaymentResult.Success success -> {
                // payment.setProcessorReference(success.bankReference());
                log.warn("Invalid state");
                return null;
            }
        }
        payment = paymentRepository.save(payment);
        publishStatusEvent(payment, "PAYMENT_CREATED");
        return paymentMapper.toResponse(payment);
    }
}
