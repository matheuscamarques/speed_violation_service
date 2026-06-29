package br.com.velsis.SpeedViolationService.controller;

import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.service.ViolationService;
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

    private final ViolationService violationService;

    public ViolationEvaluateController(ViolationService violationService) {
        this.violationService = violationService;
    }

    @PostMapping("/evaluate")
    public ResponseEntity<ViolationResponse> evaluate(
            @RequestHeader(HEADER_X_ORIGIN) String origin,
            @Valid @RequestBody CaptureRequestDTO request) {

        if (!VALID_ORIGINS.contains(origin)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        ViolationResponse response = violationService.evaluate(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<ViolationResponse>> findByLicensePlate(
            @RequestParam String licensePlate) {
        List<ViolationResponse> violations = violationService.findByLicensePlate(licensePlate);
        return ResponseEntity.ok(violations);
    }
}
