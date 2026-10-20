package com.payment_service.statemachine;

import com.payment_service.entity.Payment;
import com.payment_service.entity.PaymentTransitionLog;
import com.payment_service.repository.PaymentTransitionLogRepository;
import common_lib.context.MerchantContext;
import common_lib.enums.PaymentActor;
import common_lib.enums.PaymentEvent;
import common_lib.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
/**
 * Applies and audits payment state-machine transitions.
 */
public class PaymentTransitionService {

    private final PaymentTransitionLogRepository paymentTransitionLogRepository;
    private final PaymentStateMachine paymentStateMachine;
    private final MerchantContext merchantContext;

    /**
     * Validates the event against the state machine, changes the in-memory payment status, and persists a system-actor
     * transition record containing the before/after states and occurrence time.
     *
     * @param payment      payment entity to mutate
     * @param paymentEvent event that drives the transition
     * @return the newly assigned status
     * @throws InvalidStateTransitionException if the current state does not allow this event
     */
    public PaymentStatus apply(Payment payment, PaymentEvent paymentEvent) {
        PaymentStatus toStatus = paymentStateMachine.transition(payment.getStatus(), paymentEvent);
        PaymentTransitionLog paymentTransitionLog = PaymentTransitionLog
                .builder()
                .payment(payment)
                .event(paymentEvent)
                .fromStatus(payment.getStatus())
                .toStatus(toStatus)
                .occurredAt(Instant.now())
                .actor(PaymentActor.SYSTEM)    //TODO: Fetch merchant context to identify actor -- Spring Security
                .build();
        payment.setStatus(toStatus);    //update payment's status
        paymentTransitionLogRepository.save(paymentTransitionLog);
        return toStatus;
    }
}
