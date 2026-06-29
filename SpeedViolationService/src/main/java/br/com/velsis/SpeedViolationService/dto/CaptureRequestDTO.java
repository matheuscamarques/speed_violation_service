package br.com.velsis.SpeedViolationService.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

import java.time.OffsetDateTime;

@Schema(description = "Dados da captura de velocidade para avaliação de infração")
public record CaptureRequestDTO(
        @Schema(description = "Placa do veículo (Mercosul: ABC1D23 ou formato antigo: ABC1234)",
                example = "ABC1D23", pattern = "[A-Z]{3}[0-9][A-Z][0-9]{2}|[A-Z]{3}[0-9]{4}")
        @NotBlank @Pattern(regexp = LICENSE_PLATE_REGEX)
        String licensePlate,

        @Schema(description = "Velocidade medida pelo equipamento (km/h)", example = "92")
        @Positive double measuredSpeed,

        @Schema(description = "Velocidade máxima permitida na via (km/h)", example = "60")
        @Positive double speedLimit,

        @Schema(description = "Identificador do equipamento de medição", example = "RAD-CWB-001")
        @NotBlank String equipmentId,

        @Schema(description = "Data/hora da captura (ISO-8601)", example = "2026-06-08T14:30:00Z")
        @NotNull @PastOrPresent OffsetDateTime captureTimestamp
) {
    public static final String LICENSE_PLATE_REGEX = "[A-Z]{3}[0-9][A-Z][0-9]{2}|[A-Z]{3}[0-9]{4}";
}
