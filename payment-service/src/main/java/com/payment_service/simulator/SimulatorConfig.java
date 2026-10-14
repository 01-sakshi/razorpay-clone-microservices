package com.payment_service.simulator;

import common_lib.enums.ChaosMode;
import common_lib.enums.PaymentMethod;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "payment.simulator")
@Getter
@Setter
public class SimulatorConfig {
    //    Assign all properties with some default values
    private Integer pollIntervalMs = 2000;
    private ChaosMode chaosMode = ChaosMode.NORMAL;
    private Map<String, MethodSimulatorConfig> methods = new HashMap<>();


    //Hashmap(methods) is populated via application properties file
    /** Returns the method's configured delay/success settings, or a fresh default configuration when none is present. */
    MethodSimulatorConfig configOf(PaymentMethod paymentMethod) {
        return methods.getOrDefault(paymentMethod.name(), new MethodSimulatorConfig());
    }

    @Getter
    @Setter
    static class MethodSimulatorConfig {    //Doubt
        private Integer minDelaySeconds = 1;
        private Integer maxDelaySeconds = 5;
        private Integer successRate = 80;
    }
}
