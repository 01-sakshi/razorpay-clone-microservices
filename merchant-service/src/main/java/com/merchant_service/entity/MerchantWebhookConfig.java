package com.merchant_service.entity;

import common_lib.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.UUID;

@Entity
@Table(name = "merchant_webhook_config",
        indexes = {
                @Index(name = "idx_merchant_webhook_config", columnList = "merchant_id, enabled")
        })
@Getter
@Setter
@ToString 
@Builder 
@AllArgsConstructor 
@NoArgsConstructor 
public class MerchantWebhookConfig extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "merchant_id", nullable = false)
    @ToString.Exclude
    private Merchant merchant;

    @Column(nullable = false)
    private String webhookSecret;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    //To which merchant URL, webhook should be sent
    @Column(nullable = false, length = 100)
    private String targetUrl;   

    //For which all events, webhook should be sent to merchant
    //event types are comma separated values, for example: payment.captured,payment.failed,order.paid
    @Column(nullable = false, length = 50)
    private String eventTypes;  

    /**
     * Matches a comma-separated subscription case-insensitively; a blank subscription or {@code ALL} matches every event.
     *
     * @param eventType event name to test
     * @return whether this configuration should receive the event
     */
    public boolean isSubscribedTo(String eventType) {
        if (eventTypes == null || eventTypes.isBlank()) {
            return true;
        }
        for (String type : eventTypes.split(",")) {
            String trimmed = type.trim();
            if (trimmed.equalsIgnoreCase("ALL") || trimmed.equalsIgnoreCase(eventType)) {
                return true;
            }
        }
        return false;
    }
}
