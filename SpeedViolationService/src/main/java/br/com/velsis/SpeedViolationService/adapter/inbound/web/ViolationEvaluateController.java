package br.com.velsis.SpeedViolationService.adapter.inbound.web;

import br.com.velsis.SpeedViolationService.domain.port.inbound.ViolationEvaluationUseCase;
import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ErrorResponse;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/violations")
@Tag(name = "Violações", description = "Avaliação e consulta de infrações de velocidade (arts. 218 CTB)")
public class ViolationEvaluateController {

    private static final Set<String> VALID_ORIGINS = Set.of("FIXED", "MOBILE", "HANDHELD");
    private static final String HEADER_X_ORIGIN = "x-origin";

    private final ViolationEvaluationUseCase violationEvaluationUseCase;

    public ViolationEvaluateController(ViolationEvaluationUseCase violationEvaluationUseCase) {
        this.violationEvaluationUseCase = violationEvaluationUseCase;
    }

    @PostMapping("/evaluate")
    @Operation(summary = "Avaliar infração de velocidade",
            description = "Recebe os dados de uma captura de velocidade e retorna se houve infração, " +
                    "com a gravidade e o enquadramento no CTB.")
    @ApiResponse(responseCode = "200", description = "Avaliação concluída",
            content = @Content(schema = @Schema(implementation = ViolationResponse.class)))
    @ApiResponse(responseCode = "400", description = "Dados inválidos ou x-origin inválido",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    public ResponseEntity<ViolationResponse> evaluate(
            @Parameter(description = "Origem da captura (FIXED, MOBILE, HANDHELD)", required = true,
                    example = "FIXED")
            @RequestHeader(HEADER_X_ORIGIN) String origin,
            @Valid @RequestBody CaptureRequestDTO request) {

        if (!VALID_ORIGINS.contains(origin)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        ViolationResponse response = violationEvaluationUseCase.evaluate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Consultar infrações por placa",
            description = "Retorna o histórico de infrações registradas para uma determinada placa.")
    @ApiResponse(responseCode = "200", description = "Lista de infrações (pode ser vazia)",
            content = @Content(schema = @Schema(implementation = ViolationResponse.class)))
    public ResponseEntity<List<ViolationResponse>> findByLicensePlate(
            @Parameter(description = "Placa do veículo (Mercosul ou formato antigo)", required = true,
                    example = "ABC1D23")
            @RequestParam String licensePlate) {
        List<ViolationResponse> violations = violationEvaluationUseCase.findByLicensePlate(licensePlate);
        return ResponseEntity.ok(violations);
    }
}
