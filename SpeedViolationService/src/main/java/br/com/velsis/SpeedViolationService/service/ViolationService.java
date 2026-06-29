package br.com.velsis.SpeedViolationService.service;

import br.com.velsis.SpeedViolationService.config.SpeedViolationProperties;
import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse.ViolationDetails;
import br.com.velsis.SpeedViolationService.model.Violation;
import br.com.velsis.SpeedViolationService.model.ViolationSeverity;
import br.com.velsis.SpeedViolationService.store.ViolationStore;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ViolationService {

    private final ViolationStore violationStore;
    private final SpeedViolationProperties properties;

    public ViolationService(ViolationStore violationStore, SpeedViolationProperties properties) {
        this.violationStore = violationStore;
        this.properties = properties;
    }

    private double calculateTolerance(double speedLimit) {
        if (speedLimit > properties.getThreshold()) {
            return speedLimit * (properties.getTolerancePercentage() / 100.0);
        }
        return properties.getToleranceFixed();
    }

    public ViolationResponse evaluate(CaptureRequestDTO request) {
        double tolerance = calculateTolerance(request.speedLimit());
        double consideredSpeed = Math.max(0, request.measuredSpeed() - tolerance);
        double speedLimit = request.speedLimit();
        boolean hasViolation = consideredSpeed > speedLimit;

        if (!hasViolation) {
            return noViolationResponse(request, consideredSpeed, speedLimit);
        }

        double excess = excessPercentage(consideredSpeed, speedLimit);
        ViolationSeverity severity = classifySeverity(excess);
        OffsetDateTime now = OffsetDateTime.now();

        Violation violation = Violation.of(
                request.licensePlate(), request.equipmentId(), request.measuredSpeed(),
                consideredSpeed, speedLimit, excess, severity,
                request.captureTimestamp(), now
        );

        violationStore.save(violation);
        return toResponse(violation);
    }

    public List<ViolationResponse> findByLicensePlate(String licensePlate) {
        return violationStore.findByLicensePlate(licensePlate).stream()
                .map(ViolationService::toResponse)
                .toList();
    }

    private static ViolationResponse toResponse(Violation violation) {
        return new ViolationResponse(
                violation.licensePlate(),
                violation.equipmentId(),
                violation.measuredSpeed(),
                violation.consideredSpeed(),
                violation.speedLimit(),
                violation.excessPercentage(),
                true,
                new ViolationDetails(violation.severity().name(), violation.ctbCode()),
                violation.processedAt()
        );
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

    private static double excessPercentage(double speed, double limit) {
        return BigDecimal.valueOf((speed - limit) / limit * 100)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private ViolationSeverity classifySeverity(double excess) {
        if (excess <= properties.getExcessLimitMedium()) {
            return ViolationSeverity.MEDIUM;
        }
        if (excess <= properties.getExcessLimitSerious()) {
            return ViolationSeverity.SERIOUS;
        }
        return ViolationSeverity.VERY_SERIOUS;
    }
}
