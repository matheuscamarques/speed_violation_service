package br.com.velsis.SpeedViolationService.domain.port.outbound;

import br.com.velsis.SpeedViolationService.domain.model.Violation;

import java.util.List;

public interface ViolationRepository {
    void save(Violation violation);
    List<Violation> findByLicensePlate(String licensePlate);
}
