package com.operations_service.entity;

import common_lib.entity.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(
        name = "dlq_event"
)
@Getter
@Setter
@ToString 
@Builder
@AllArgsConstructor 
@NoArgsConstructor  
public class DlqEvent extends BaseAuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "webhook_event_id")
    @ToString.Exclude
    private WebhookEvent webhookEvent;

    private UUID merchantId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> payload;

    private String finalError;

    private Instant movedAt;

    private Instant replayedAt;
}
