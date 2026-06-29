package br.com.velsis.SpeedViolationService.adapter.outbound.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ViolationJpaRepository extends JpaRepository<ViolationEntity, UUID> {

    List<ViolationEntity> findByLicensePlate(String licensePlate);
}
