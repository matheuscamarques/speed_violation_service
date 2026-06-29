package br.com.velsis.SpeedViolationService.controller;

import br.com.velsis.SpeedViolationService.adapter.inbound.web.ViolationEvaluateController;
import br.com.velsis.SpeedViolationService.domain.port.inbound.ViolationEvaluationUseCase;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ViolationEvaluateControllerTest {

    private MockMvc mockMvc;
    private ViolationEvaluationUseCase violationEvaluationUseCase;

    private final String validBody = """
            {"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
            """;

    @BeforeEach
    void setUp() {
        violationEvaluationUseCase = mock(ViolationEvaluationUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ViolationEvaluateController(violationEvaluationUseCase)).build();
    }

    @Test
    @DisplayName("should return 200 with valid FIXED origin")
    void validRequestFixed() throws Exception {
        ViolationResponse response = new ViolationResponse("ABC1D23", "RAD-CWB-001", 92, 85, 60, 41.67, true,
                new ViolationResponse.ViolationDetails("SERIOUS", "218-II"), OffsetDateTime.now());
        when(violationEvaluationUseCase.evaluate(any())).thenReturn(response);

        performPost("FIXED", validBody)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$.violation.severity").value("SERIOUS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MOBILE", "HANDHELD"})
    @DisplayName("should return 200 with valid origins")
    void validRequestOtherOrigins(String origin) throws Exception {
        ViolationResponse response = new ViolationResponse("ABC1D23", "RAD-CWB-001", 92, 85, 60, 41.67, true,
                new ViolationResponse.ViolationDetails("SERIOUS", "218-II"), OffsetDateTime.now());
        when(violationEvaluationUseCase.evaluate(any())).thenReturn(response);

        performPost(origin, validBody).andExpect(status().isOk());
    }

    @Test
    @DisplayName("should return 400 when x-origin is invalid")
    void invalidOrigin() throws Exception {
        performPost("INVALID", validBody).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when x-origin is missing")
    void missingOrigin() throws Exception {
        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when x-origin is empty")
    void emptyOrigin() throws Exception {
        performPost("", validBody).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when license plate format is invalid")
    void invalidLicensePlate() throws Exception {
        String body = """
                {"licensePlate":"INVALID","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when measuredSpeed is negative")
    void negativeSpeed() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":-10,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when measuredSpeed is zero")
    void zeroSpeed() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":0,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when speedLimit is zero")
    void zeroSpeedLimit() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":0,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when licensePlate is empty")
    void emptyLicensePlate() throws Exception {
        String body = """
                {"licensePlate":"","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when equipmentId is empty")
    void emptyEquipmentId() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 415 when content type is not JSON")
    void wrongContentType() throws Exception {
        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_XML)
                        .content(validBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @DisplayName("should return 400 when licensePlate field is missing")
    void missingLicensePlate() throws Exception {
        String body = """
                {"measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when measuredSpeed field is missing")
    void missingMeasuredSpeed() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when speedLimit field is missing")
    void missingSpeedLimit() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when equipmentId field is missing")
    void missingEquipmentId() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when captureTimestamp field is missing")
    void missingCaptureTimestamp() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when speedLimit is negative")
    void negativeSpeedLimit() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":-1,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when captureTimestamp is not ISO-8601")
    void invalidCaptureTimestampFormat() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"08-06-2026"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when captureTimestamp is in the future")
    void futureCaptureTimestamp() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2099-01-01T00:00:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RF6.6: should return 400 when captureTimestamp is null")
    void nullCaptureTimestamp() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":null}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when x-origin is lowercase")
    void invalidOriginCase() throws Exception {
        performPost("fixed", validBody).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("RF6.3: should return 200 with valid old format license plate (ABC1234)")
    void validLicensePlateOldFormat() throws Exception {
        ViolationResponse response = new ViolationResponse("ABC1234", "RAD-CWB-001", 50, 43, 60, 0, false, null, OffsetDateTime.now());
        when(violationEvaluationUseCase.evaluate(any())).thenReturn(response);

        String body = """
                {"licensePlate":"ABC1234","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isOk());
    }

    @Nested
    @DisplayName("GET /api/v1/violations?licensePlate=")
    class FindByLicensePlate {

        @Test
        @DisplayName("should return 200 with empty list when no violations")
        void noViolations() throws Exception {
            when(violationEvaluationUseCase.findByLicensePlate("ABC1D23")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/violations")
                            .param("licensePlate", "ABC1D23"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }

        @Test
        @DisplayName("should return 200 with violations list")
        void withViolations() throws Exception {
            ViolationResponse violation = new ViolationResponse("ABC1D23", "RAD-001", 100, 93, 80, 16.25, true,
                    new ViolationResponse.ViolationDetails("MEDIUM", "218-I"), OffsetDateTime.now());
            when(violationEvaluationUseCase.findByLicensePlate("ABC1D23")).thenReturn(List.of(violation));

            mockMvc.perform(get("/api/v1/violations")
                            .param("licensePlate", "ABC1D23"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].licensePlate").value("ABC1D23"))
                    .andExpect(jsonPath("$[0].violation.severity").value("MEDIUM"));
        }

        @Test
        @DisplayName("should return 200 with empty list when plate has no violations")
        void plateWithoutViolations() throws Exception {
            when(violationEvaluationUseCase.findByLicensePlate("ZZZ0000")).thenReturn(List.of());

            mockMvc.perform(get("/api/v1/violations")
                            .param("licensePlate", "ZZZ0000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    private ResultActions performPost(String origin, String body) throws Exception {
        return mockMvc.perform(post("/api/v1/violations/evaluate")
                .header("x-origin", origin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
