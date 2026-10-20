package com.operations_service.settlement;

import java.util.List;

import com.operations_service.entity.Settlement;
import com.operations_service.repository.SettlementRepository;
import common_lib.enums.SettlementStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class BankSettlementCallbackSimulator {

    private final SettlementRepository settlementRepository;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    /** Finds settlements still awaiting transfer and invokes the simulator for each; an empty batch is a no-op. */
//    @Scheduled(fixedDelay = 5000)
    public void processCallbacks() {
        List<Settlement> settlements = settlementRepository.findByStatus(SettlementStatus.TRANSFER_PENDING);
        if (settlements.isEmpty())
            return;
        for (Settlement settlement : settlements) {
            simulateCallback(settlement);
        }
    }

    /** Feeds a successful simulated bank result to the transaction executor for the supplied pending settlement. */
    public void simulateCallback(Settlement settlement) {
        log.info("Initiating settlement callback for settlement id : {}", settlement.getId());
        settlementTransactionExecutor.resolveTransfer(settlement.getId(), null, null);
    }
}
