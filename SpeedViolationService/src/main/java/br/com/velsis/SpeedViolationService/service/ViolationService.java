package br.com.velsis.SpeedViolationService.service;

import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse.ViolationDetails;
import br.com.velsis.SpeedViolationService.model.ViolationSeverity;
import br.com.velsis.SpeedViolationService.store.ViolationStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ViolationService {

    private static final double TOLERANCE = 7.0;
    private static final double EXCESS_LIMIT_MEDIUM = 20.0;
    private static final double EXCESS_LIMIT_SERIOUS = 50.0;

    private final ViolationStore violationStore;

    public ViolationService(ViolationStore violationStore) {
        this.violationStore = violationStore;
    }

    public ViolationResponse evaluate(CaptureRequestDTO request) {
        var consideredSpeed = Math.max(0, request.measuredSpeed() - TOLERANCE);
        var speedLimit = request.speedLimit();
        var hasViolation = consideredSpeed > speedLimit;
        var excess = hasViolation ? excessPercentage(consideredSpeed, speedLimit) : 0;
        var details = hasViolation ? classifyViolation(excess) : null;

        var response = new ViolationResponse(
                request.licensePlate(),
                request.equipmentId(),
                request.measuredSpeed(),
                consideredSpeed,
                speedLimit,
                excess,
                hasViolation,
                details,
                OffsetDateTime.now()
        );

        if (hasViolation) {
            violationStore.save(response);
        }

        return response;
    }

    public List<ViolationResponse> findByLicensePlate(String licensePlate) {
        return violationStore.findByLicensePlate(licensePlate);
    }

    private static double excessPercentage(double speed, double limit) {
        return BigDecimal.valueOf((speed - limit) / limit * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static ViolationDetails classifyViolation(double excess) {
        var severity = excess <= EXCESS_LIMIT_MEDIUM ? ViolationSeverity.MEDIUM
                     : excess <= EXCESS_LIMIT_SERIOUS ? ViolationSeverity.SERIOUS
                     : ViolationSeverity.GRAVE;
        return new ViolationDetails(severity.name(), severity.ctbCode());
    }
}
