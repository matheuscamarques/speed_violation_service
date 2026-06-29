package br.com.velsis.SpeedViolationService.controller;

import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.service.ViolationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ViolationEvaluateControllerTest {

    private MockMvc mockMvc;
    private ViolationService violationService;

    private final String validBody = """
            {"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
            """;

    @BeforeEach
    void setUp() {
        violationService = mock(ViolationService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new ViolationEvaluateController(violationService)).build();
    }

    @Test
    @DisplayName("should return 200 with valid FIXED origin")
    void validRequestFixed() throws Exception {
        var response = new ViolationResponse("ABC1D23", "RAD-CWB-001", 92, 85, 60, 41.67, true,
                new ViolationResponse.ViolationDetails("SERIOUS", "218-II"), OffsetDateTime.now());
        when(violationService.evaluate(any())).thenReturn(response);

        performPost("FIXED", validBody)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$.violation.severity").value("SERIOUS"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"MOBILE", "HANDHELD"})
    @DisplayName("should return 200 with valid origins")
    void validRequestOtherOrigins(String origin) throws Exception {
        var response = new ViolationResponse("ABC1D23", "RAD-CWB-001", 92, 85, 60, 41.67, true,
                new ViolationResponse.ViolationDetails("SERIOUS", "218-II"), OffsetDateTime.now());
        when(violationService.evaluate(any())).thenReturn(response);

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
        var body = """
                {"licensePlate":"INVALID","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when measuredSpeed is negative")
    void negativeSpeed() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":-10,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when measuredSpeed is zero")
    void zeroSpeed() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":0,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when speedLimit is zero")
    void zeroSpeedLimit() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":0,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when licensePlate is empty")
    void emptyLicensePlate() throws Exception {
        var body = """
                {"licensePlate":"","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when equipmentId is empty")
    void emptyEquipmentId() throws Exception {
        var body = """
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
        var body = """
                {"measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when measuredSpeed field is missing")
    void missingMeasuredSpeed() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when speedLimit field is missing")
    void missingSpeedLimit() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when equipmentId field is missing")
    void missingEquipmentId() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when captureTimestamp field is missing")
    void missingCaptureTimestamp() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when speedLimit is negative")
    void negativeSpeedLimit() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":-1,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when captureTimestamp is not ISO-8601")
    void invalidCaptureTimestampFormat() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"08-06-2026"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when captureTimestamp is in the future")
    void futureCaptureTimestamp() throws Exception {
        var body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2099-01-01T00:00:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 400 when x-origin is lowercase")
    void invalidOriginCase() throws Exception {
        performPost("fixed", validBody).andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("should return 200 with valid old format license plate (ABC1234)")
    void validLicensePlateOldFormat() throws Exception {
        var response = new ViolationResponse("ABC1234", "RAD-CWB-001", 50, 43, 60, 0, false, null, OffsetDateTime.now());
        when(violationService.evaluate(any())).thenReturn(response);

        var body = """
                {"licensePlate":"ABC1234","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;
        performPost("FIXED", body).andExpect(status().isOk());
    }

    private ResultActions performPost(String origin, String body) throws Exception {
        return mockMvc.perform(post("/api/v1/violations/evaluate")
                .header("x-origin", origin)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
