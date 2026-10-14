package com.operations_service.entity;

import common_lib.entity.BaseAuditEntity;
import common_lib.enums.WebhookEventStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "webhook_event"
)
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WebhookEvent extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID merchantId;

    @Column(nullable = false)
    private String eventType;

    @Enumerated(EnumType.STRING)
    private WebhookEventStatus eventStatus;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    @Column(nullable = false)
    @Builder.Default
    private Integer attempts = 0;

    private String signature;

    private String targetUrl;

    private Instant nextRetryAt;

    private Instant lastAttemptAt;

    private Integer lastResponseCode;

    @Column(length = 1000)
    private String lastResponseBody;

    private Instant deliveredAt;
}
