package com.operations_service.settlement;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.operations_service.client.MerchantServiceClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class SettlementEngine {

    private final MerchantServiceClient merchantServiceClient;
    private final SettlementTransactionExecutor settlementTransactionExecutor;

    /**
     * Invokes the all-merchant settlement batch at 23:00 server-local time each
     * day.
     */
    @Scheduled(cron = "*/30 * * * * *") // for testing, every 30 seconds
    // @Scheduled(cron = "0 0 23 * * *")
    public void runScheduled() {
        log.info("Nightly settlement scheduler running");
        run();
    }

    /**
     * Submits one settlement transaction per active merchant on virtual threads and
     * waits for all tasks to finish.
     * Individual task failures are logged and do not abort processing of the
     * remaining merchants.
     */
    public void run() {
        List<UUID> activeMerchants = merchantServiceClient.getAllActiveMerchantIds();
        log.info("Processing the settlements for merchant ids: {}", activeMerchants.size());

        try (ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>(); // Future<?> ??

            for (UUID merchantId : activeMerchants) {
                futures.add(executorService.submit(() -> {
                    settlementTransactionExecutor.processForMerchant(merchantId, Instant.now());
                }));
            }

            for (var f : futures) {
                try {
                    f.get();    //blocking
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Settlement batch future failed ", e);
                }
            }
        }
        log.info("Settlement batch completed");
    }
}
