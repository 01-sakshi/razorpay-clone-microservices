package com.operations_service.repository;

import java.util.UUID;

import com.operations_service.entity.DlqEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DlqEventRepository extends JpaRepository<DlqEvent, UUID> {

}
