package br.com.velsis.SpeedViolationService.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record Violation(
        UUID id,
        String licensePlate,
        String equipmentId,
        double measuredSpeed,
        double consideredSpeed,
        double speedLimit,
        double excessPercentage,
        ViolationSeverity severity,
        String ctbCode,
        OffsetDateTime captureTimestamp,
        OffsetDateTime processedAt
) {
    public static Violation of(String licensePlate, String equipmentId, double measuredSpeed,
                                double consideredSpeed, double speedLimit, double excessPercentage,
                                ViolationSeverity severity, OffsetDateTime captureTimestamp,
                                OffsetDateTime processedAt) {
        return new Violation(
                UUID.randomUUID(),
                licensePlate, equipmentId, measuredSpeed, consideredSpeed,
                speedLimit, excessPercentage, severity, severity.ctbCode(),
                captureTimestamp, processedAt
        );
    }
}
