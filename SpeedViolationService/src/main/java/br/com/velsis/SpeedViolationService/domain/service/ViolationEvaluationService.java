package br.com.velsis.SpeedViolationService.domain.service;

import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.model.ViolationSeverity;
import br.com.velsis.SpeedViolationService.domain.port.inbound.ViolationEvaluationUseCase;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationConfigPort;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationRepository;
import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse.ViolationDetails;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.List;

public class ViolationEvaluationService implements ViolationEvaluationUseCase {

    private final ViolationRepository violationRepository;
    private final ViolationConfigPort config;

    public ViolationEvaluationService(ViolationRepository violationRepository, ViolationConfigPort config) {
        this.violationRepository = violationRepository;
        this.config = config;
    }

    private double calculateTolerance(double speedLimit) {
        if (speedLimit > config.getThreshold()) {
            return speedLimit * (config.getTolerancePercentage() / 100.0);
        }
        return config.getToleranceFixed();
    }

    @Override
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

        violationRepository.save(violation);
        return toResponse(violation);
    }

    @Override
    public List<ViolationResponse> findByLicensePlate(String licensePlate) {
        return violationRepository.findByLicensePlate(licensePlate).stream()
                .map(ViolationEvaluationService::toResponse)
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
        if (excess <= config.getExcessLimitMedium()) {
            return ViolationSeverity.MEDIUM;
        }
        if (excess <= config.getExcessLimitSerious()) {
            return ViolationSeverity.SERIOUS;
        }
        return ViolationSeverity.VERY_SERIOUS;
    }
}
