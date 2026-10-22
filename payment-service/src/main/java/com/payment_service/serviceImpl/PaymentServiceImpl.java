package com.payment_service.serviceImpl;

import com.payment_service.dto.request.PaymentInitRequest;
import com.payment_service.dto.response.PaymentResponse;
import com.payment_service.entity.OrderRecord;
import com.payment_service.entity.Payment;
import com.payment_service.gateway.PaymentGatewayRouter;
import com.payment_service.gateway.dto.PaymentRequest;
import com.payment_service.gateway.dto.PaymentResult;
import com.payment_service.mapper.PaymentMapper;
import com.payment_service.outbox.OutboxEventPublisher;
import com.payment_service.repository.OrderRepository;
import com.payment_service.repository.PaymentRepository;
import com.payment_service.saga.PaymentAuthorizationRecorder;
import com.payment_service.service.PaymentService;
import com.payment_service.statemachine.PaymentTransitionService;
import common_lib.enums.EventAggregateType;
import common_lib.enums.OrderStatus;
import common_lib.enums.PaymentEvent;
import common_lib.enums.PaymentStatus;
import common_lib.exceptions.BusinessRuleViolationException;
import common_lib.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGatewayRouter paymentGatewayRouter;
    private final PaymentMapper paymentMapper;
    private final OutboxEventPublisher eventPublisher;

    private final PaymentTransitionService paymentTransitionService;
    private final PaymentAuthorizationRecorder paymentAuthorizationRecorder;

    /**
     * Locks the merchant-owned order to serialize attempts, permits only {@code CREATED}/{@code ATTEMPTED} orders,
     * increments the attempt count, and persists the routed authorization transition and outbox event. A gateway
     * failure moves the payment to failed; an unexpected immediate-success result currently returns {@code null}.
     *
     * @param merchantId merchant that owns the order and payment attempt
     * @param request    order identifier, payment method, and method-specific details
     * @return the persisted payment representation, or {@code null} for the current immediate-success branch
     * @throws ResourceNotFoundException      if the order is absent or belongs to another merchant
     * @throws BusinessRuleViolationException if the order is not payable
     */
    @Override
    @Transactional
    public PaymentResponse initiate(UUID merchantId, PaymentInitRequest request) {
        // Orchestration SAGA Pattern is applied here
        Payment payment = paymentAuthorizationRecorder.recordPayment(merchantId, request);
        PaymentResult paymentResult = null;
        try {
            paymentResult = paymentGatewayRouter.initiate(new PaymentRequest(
                    payment.getId(),
                    merchantId,
                    request.orderId(),
                    payment.getMethod(),
                    payment.getMethodDetails(),
                    payment.getAmount()));
        } catch (Exception e) {
            paymentAuthorizationRecorder.compensateAuthorizationFailure(payment.getId(),
                    "PAYMENT_GATEWAY_ROUTER_UNREACHABLE", e.getMessage());
        }
        return paymentAuthorizationRecorder.applyGatewayResult(payment.getId(), paymentResult);
    }

    /**
     * Locks the payment within the merchant scope, applies the capture-request transition, routes capture, persists
     * success or failure details, and writes {@code PAYMENT_STATUS_CHANGED} to the outbox.
     *
     * @param merchantId merchant that owns the payment
     * @param paymentId  identifier of the payment to capture
     * @return the persisted payment representation after the gateway result
     * @throws ResourceNotFoundException       if the payment is absent or belongs to another merchant
     * @throws InvalidStateTransitionException if its current state cannot enter capture
     */
    @Override
    @Transactional
    public PaymentResponse capture(UUID merchantId, UUID paymentId) {
        // Payment payment = paymentRepository.findByIdAndMerchantId(paymentId,
        // merchantId)
        // .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", paymentId));

        Payment payment = paymentRepository.findByIdAndMerchantIdForUpdate(paymentId, merchantId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", paymentId));

        // payment.setStatus(PaymentStatus.CAPTURING);
        paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);

        // pass request to PaymentGatewayRouter to get the payment captured.
        PaymentResult paymentResult = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

        if (paymentResult instanceof PaymentResult.Failure failure) {
            // payment.setStatus(PaymentStatus.AUTHORIZED);
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
            payment.setErrorCode(failure.errorCode());
            payment.setErrorDescription(failure.errorDescription());
            log.error("Payment captured failed, paymentId: {}", paymentId);
        } else if (paymentResult instanceof PaymentResult.Success success) {
            // payment.setStatus(PaymentStatus.CAPTURED);
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
            payment.setCapturedAt(Instant.now());
            log.info("Payment captured, paymentId: {}", paymentId);
        }

        payment = paymentRepository.save(payment);

        eventPublisher.publish(EventAggregateType.PAYMENT, payment.getId(), "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", payment.getOrderRecord().getId(),
                        "paymentId", payment.getId(),
                        "merchantId", merchantId.toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()));

        return paymentMapper.toResponse(payment);
    }

    /**
     * Locks the payment and applies the authorization result; approvals proceed immediately through capture, and a
     * successful capture also marks the order paid. The final payment/order state is persisted and published.
     *
     * @param paymentId        identifier of the payment whose authorization result was received
     * @param approve          whether authorization succeeded
     * @param bankRef          bank reference supplied by the callback
     * @param errorCode        gateway error code
     * @param errorDescription gateway error description
     * @throws ResourceNotFoundException if the payment does not exist
     */
    @Override
    @Transactional
    public void resolveAuthorization(UUID paymentId, boolean approve, String bankRef, String errorCode,
                                     String errorDescription) {
        // Payment payment = paymentRepository.findById(paymentId).orElseThrow(() ->
        // new ResourceNotFoundException("PAYMENT", paymentId));

        Payment payment = paymentRepository.findByIdForUpdate(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("PAYMENT", paymentId));

        if (!payment.getStatus().equals(PaymentStatus.AUTHORIZING)) {
            log.warn("Payment is not in authorizing state, paymentId: {}, status: {}", paymentId, payment.getStatus());
            return;
        }
        OrderRecord orderRecord = payment.getOrderRecord();

        /* At this point, payment is in Authorizing state */
        if (approve) {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_SUCCESS);
            payment.setBankReference(bankRef);
            payment.setAuthorizedAt(Instant.now());

            // Auto-capture payment flow
            paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_REQUEST);
            // Send request to payment gateway router to get the payment captured
            PaymentResult capture = paymentGatewayRouter.capture(payment.getMethod(), paymentId);

            if (capture instanceof PaymentResult.Success success) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_SUCCESS);
                payment.setCapturedAt(Instant.now());
                orderRecord.setStatus(OrderStatus.PAID);
            } else if (capture instanceof PaymentResult.Failure failure) {
                paymentTransitionService.apply(payment, PaymentEvent.CAPTURE_FAIL);
                payment.setErrorCode(failure.errorCode());
                payment.setErrorDescription(failure.errorDescription());
            }
        } else {
            paymentTransitionService.apply(payment, PaymentEvent.AUTHORIZE_FAIL);
            payment.setErrorCode(errorCode);
            payment.setErrorDescription(errorDescription);
        }

        /*
         * Dirty-Checking may work here leading to orderRecord getting updated as part
         * of
         * paymentRepository.save(payment) itself, leading to
         * orderRepository.save(orderRecord)
         * not getting executed at all
         */
        payment = paymentRepository.save(payment);
        orderRecord = orderRepository.save(orderRecord);

        eventPublisher.publish(EventAggregateType.PAYMENT, paymentId, "PAYMENT_STATUS_CHANGED",
                Map.of("orderId", orderRecord.getId().toString(),
                        "paymentId", paymentId.toString(),
                        "merchantId", payment.getMerchantId().toString(),
                        "paymentStatus", payment.getStatus().name(),
                        "amountUnits", payment.getAmount().getAmountUnits(),
                        "amountCurrency", payment.getAmount().getCurrency(),
                        "paymentMethod", payment.getMethod()));
    }
}
