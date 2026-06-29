package br.com.velsis.SpeedViolationService.domain.port.inbound;

import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;

import java.util.List;

public interface ViolationEvaluationUseCase {
    ViolationResponse evaluate(CaptureRequestDTO request);
    List<ViolationResponse> findByLicensePlate(String licensePlate);
}
