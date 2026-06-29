package br.com.velsis.SpeedViolationService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CaptureRequestDTO(
        @NotBlank @Pattern(regexp = "[A-Z]{3}[0-9][A-Z][0-9]{2}|[A-Z]{3}[0-9]{4}")
        String licensePlate,
        @Positive double measuredSpeed,
        @Positive double speedLimit,
        @NotBlank String equipmentId,
        @NotBlank String captureTimestamp
) {
}
