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
        double consideredSpeed = Math.max(0, request.measuredSpeed() - TOLERANCE);
        double speedLimit = request.speedLimit();
        boolean hasViolation = consideredSpeed > speedLimit;

        if (!hasViolation) {
            return noViolationResponse(request, consideredSpeed, speedLimit);
        }

        double excess = excessPercentage(consideredSpeed, speedLimit);
        ViolationDetails details = classifyViolation(excess);
        ViolationResponse response = violationResponse(request, consideredSpeed, speedLimit, excess, details);
        violationStore.save(response);
        return response;
    }

    public List<ViolationResponse> findByLicensePlate(String licensePlate) {
        return violationStore.findByLicensePlate(licensePlate);
    }

    private static ViolationResponse noViolationResponse(CaptureRequestDTO request, double consideredSpeed, double speedLimit) {
        return new ViolationResponse(
                request.licensePlate(),
                request.equipmentId(),
                request.measuredSpeed(),
                consideredSpeed,
                speedLimit,
                0,
                false,
                null,
                OffsetDateTime.now()
        );
    }

    private static ViolationResponse violationResponse(CaptureRequestDTO request, double consideredSpeed,
                                                        double speedLimit, double excess, ViolationDetails details) {
        return new ViolationResponse(
                request.licensePlate(),
                request.equipmentId(),
                request.measuredSpeed(),
                consideredSpeed,
                speedLimit,
                excess,
                true,
                details,
                OffsetDateTime.now()
        );
    }

    private static double excessPercentage(double speed, double limit) {
        return BigDecimal.valueOf((speed - limit) / limit * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private static ViolationDetails classifyViolation(double excess) {
        if (excess <= EXCESS_LIMIT_MEDIUM) {
            return new ViolationDetails(ViolationSeverity.MEDIUM.name(), ViolationSeverity.MEDIUM.ctbCode());
        }
        if (excess <= EXCESS_LIMIT_SERIOUS) {
            return new ViolationDetails(ViolationSeverity.SERIOUS.name(), ViolationSeverity.SERIOUS.ctbCode());
        }
        return new ViolationDetails(ViolationSeverity.GRAVE.name(), ViolationSeverity.GRAVE.ctbCode());
    }
}
