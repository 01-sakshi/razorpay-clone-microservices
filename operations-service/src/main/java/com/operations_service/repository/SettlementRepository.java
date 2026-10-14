package com.operations_service.repository;

import java.util.List;
import java.util.UUID;

import com.operations_service.entity.Settlement;
import common_lib.enums.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SettlementRepository extends JpaRepository<Settlement, UUID> {

    /** Selects settlements in the requested state for bank callback simulation or operational processing. */
    List<Settlement> findByStatus(SettlementStatus transferPending);

}
