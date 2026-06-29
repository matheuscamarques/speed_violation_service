package br.com.velsis.SpeedViolationService.adapter.inbound.web;

import br.com.velsis.SpeedViolationService.domain.port.inbound.ViolationEvaluationUseCase;
import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/violations")
public class ViolationEvaluateController {

    private static final Set<String> VALID_ORIGINS = Set.of("FIXED", "MOBILE", "HANDHELD");
    private static final String HEADER_X_ORIGIN = "x-origin";

    private final ViolationEvaluationUseCase violationEvaluationUseCase;

    public ViolationEvaluateController(ViolationEvaluationUseCase violationEvaluationUseCase) {
        this.violationEvaluationUseCase = violationEvaluationUseCase;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ViolationResponse> evaluate(
            @RequestHeader(HEADER_X_ORIGIN) String origin,
            @Valid @RequestBody CaptureRequestDTO request) {

        if (!VALID_ORIGINS.contains(origin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ViolationResponse response = violationEvaluationUseCase.evaluate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ViolationResponse>> findByLicensePlate(
            @RequestParam String licensePlate) {
        List<ViolationResponse> violations = violationEvaluationUseCase.findByLicensePlate(licensePlate);
        return ResponseEntity.ok(violations);
    }
}
