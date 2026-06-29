package br.com.velsis.SpeedViolationService.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.OffsetDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "Resposta de erro padronizada")
public record ErrorResponse(
        @Schema(description = "Código HTTP do erro", example = "400") int status,
        @Schema(description = "Categoria do erro", example = "Validation failed") String error,
        @Schema(description = "Detalhamento do erro") String message,
        @Schema(description = "Endpoint que gerou o erro", example = "/api/v1/violations/evaluate") String path,
        @Schema(description = "Data/hora do erro (ISO-8601)") OffsetDateTime timestamp
) {}
