package br.com.velsis.SpeedViolationService.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

public record CaptureRequestDTO(
        @NotBlank @Pattern(regexp = LICENSE_PLATE_REGEX)
        String licensePlate,
        @Positive double measuredSpeed,
        @Positive double speedLimit,
        @NotBlank String equipmentId,
        @NotNull @PastOrPresent OffsetDateTime captureTimestamp
) {
    public static final String LICENSE_PLATE_REGEX = "[A-Z]{3}[0-9][A-Z][0-9]{2}|[A-Z]{3}[0-9]{4}";
}
