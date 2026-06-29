package br.com.velsis.SpeedViolationService.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ViolationResponse(
        String licensePlate,
        String equipmentId,
        double measuredSpeed,
        double consideredSpeed,
        double speedLimit,
        double excessPercentage,
        boolean hasViolation,
        ViolationDetails violation,
        OffsetDateTime processedAt
) {
    public record ViolationDetails(
            String severity,
            String ctbCode
    ) {}
}
