package br.com.velsis.SpeedViolationService.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resultado da avaliação de infração de velocidade")
public record ViolationResponse(
        @Schema(description = "Placa do veículo", example = "ABC1D23") String licensePlate,
        @Schema(description = "Identificador do equipamento", example = "RAD-CWB-001") String equipmentId,
        @Schema(description = "Velocidade medida (km/h)", example = "92") double measuredSpeed,
        @Schema(description = "Velocidade considerada após tolerância (km/h)", example = "85") double consideredSpeed,
        @Schema(description = "Velocidade máxima permitida (km/h)", example = "60") double speedLimit,
        @Schema(description = "Percentual de excesso em relação ao limite", example = "41.67") double excessPercentage,
        @Schema(description = "Indica se houve infração") boolean hasViolation,
        @Schema(description = "Detalhes da infração (null se não houve infração)")
        ViolationDetails violation,
        @Schema(description = "Data/hora do processamento (ISO-8601)") OffsetDateTime processedAt
) {
    @Schema(description = "Detalhes da infração conforme o CTB")
    public record ViolationDetails(
            @Schema(description = "Gravidade da infração (MEDIUM, SERIOUS, VERY_SERIOUS)",
                    example = "SERIOUS") String severity,
            @Schema(description = "Artigo do CTB (218-I, 218-II, 218-III)",
                    example = "218-II") String ctbCode
    ) {}
}
