package com.operations_service.repository;

import com.operations_service.entity.SettlementPayment;
import com.operations_service.entity.SettlementPaymentId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SettlementPaymentRepository extends JpaRepository<SettlementPayment, SettlementPaymentId> {
	List<SettlementPayment> findAllBySettlementPaymentId_SettlementId(UUID settlementId);

}
