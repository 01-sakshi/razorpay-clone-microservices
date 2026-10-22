package com.payment_service.simulator;

import com.payment_service.entity.Payment;
import com.payment_service.repository.PaymentRepository;
import com.payment_service.service.PaymentService;
import common_lib.enums.ChaosMode;
import common_lib.enums.PaymentStatus;
import common_lib.util.RandomizerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

import static common_lib.enums.ChaosMode.SLOW;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankCallbackSimulator {    //This is Payment Gateway's Simulated Bank
    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final SimulatorConfig simulatorConfig;

    /**
     * Polls payments left in {@code AUTHORIZING} for at least one second and evaluates their configured callbacks.
     */
    @Scheduled(fixedDelayString = "${payment.simulator.poll-interval-ms:5000}")
    @SchedulerLock(name = "payment-service-bank-callback-simulator", lockAtLeastFor = "1s", lockAtMostFor = "10s")
    public void processCallbacks() {
        Instant globalWindow = Instant.now().minusSeconds(1);   //Take all payments in AUTHORIZING status till (current time - 1 second)
        List<Payment> candidates = paymentRepository.findByStatusAndUpdatedAtBefore(PaymentStatus.AUTHORIZING,
                globalWindow); //Doubt: CreatedAt or UpdatedAt?

        if (candidates.isEmpty()) return;
        for (Payment payment : candidates) {
            simulateCallback(payment);
        }
    }

    /**
     * Waits until the deterministic method-specific due time, then applies the global chaos mode or success rate.
     */
    private void simulateCallback(Payment payment) {
        SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig = simulatorConfig.configOf(payment.getMethod());

        Instant isDue = dueAt(payment, methodSimulatorConfig);
        if (Instant.now().isBefore(isDue)) return;

        switch (simulatorConfig.getChaosMode()) {
            case ChaosMode.FAILURE -> resolve(payment, false);
            case ChaosMode.SUCCESS -> resolve(payment, true);
            case ChaosMode.NORMAL, SLOW -> resolve(payment, shouldApprove(payment, methodSimulatorConfig));
            case ChaosMode.TIMEOUT -> {
                log.error("Payment timed out, payment id: {}", payment.getId());
            }
        }
    }

    /**
     * Sends a generated bank reference and either approval or the configured simulated decline to payment resolution.
     */
    private void resolve(Payment payment, boolean approve) {
        String bank_ref = "SIM_BANK_REF" + RandomizerUtil.randomBase64(8);
        if (approve) {
            //This will be an external API in real life scenario
            paymentService.resolveAuthorization(payment.getId(), true, bank_ref, null, null);
        } else {
            paymentService.resolveAuthorization(payment.getId(), false, bank_ref, "SIM_BANK_ERROR_CODE",
                    "Simulated bank declined");
        }
    }

    /**
     * Maps the payment UUID hash to a stable 0-99 bucket and approves when it falls below the configured percentage.
     */
    private boolean shouldApprove(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig) {
        int bucket = Math.abs(payment.getId().hashCode()) % 100;
        return methodSimulatorConfig.getSuccessRate() > bucket; //success scenario
    }

    /**
     * Derives an inclusive min/max delay from the payment UUID hash, doubles it in slow mode, and adds it to updatedAt.
     */
    private Instant dueAt(Payment payment, SimulatorConfig.MethodSimulatorConfig methodSimulatorConfig) {
        int range = methodSimulatorConfig.getMaxDelaySeconds() - methodSimulatorConfig.getMinDelaySeconds();
        int delaySeconds = methodSimulatorConfig.getMinDelaySeconds() + (Math.abs(payment.getId().hashCode()) % (range + 1));

        if (simulatorConfig.getChaosMode().equals(SLOW)) delaySeconds *= 2;
        return payment.getUpdatedAt().plusSeconds(delaySeconds);    //or CreatedAt?
    }
}
