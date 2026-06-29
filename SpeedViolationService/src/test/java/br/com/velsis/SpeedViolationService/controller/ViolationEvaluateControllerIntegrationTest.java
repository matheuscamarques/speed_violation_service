package br.com.velsis.SpeedViolationService.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
class ViolationEvaluateControllerIntegrationTest {

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    @DisplayName("POST: should return 200 with violation when speed exceeds limit")
    void violation() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasViolation").value(true))
                .andExpect(jsonPath("$.licensePlate").value("ABC1D23"))
                .andExpect(jsonPath("$.equipmentId").value("RAD-CWB-001"))
                .andExpect(jsonPath("$.measuredSpeed").value(92))
                .andExpect(jsonPath("$.speedLimit").value(60))
                .andExpect(jsonPath("$.consideredSpeed").value(85))
                .andExpect(jsonPath("$.excessPercentage").value(41.67))
                .andExpect(jsonPath("$.violation.severity").value("SERIOUS"))
                .andExpect(jsonPath("$.violation.ctbCode").value("218-II"))
                .andExpect(jsonPath("$.processedAt").isNotEmpty());
    }

    @Test
    @DisplayName("POST: should return 200 with no violation when speed is within tolerance")
    void noViolation() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":60,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.hasViolation").value(false))
                .andExpect(jsonPath("$.excessPercentage").value(0))
                .andExpect(jsonPath("$.violation").doesNotExist());
    }

    @Test
    @DisplayName("POST: should return 200 with old format license plate")
    void oldFormatPlate() throws Exception {
        String body = """
                {"licensePlate":"ABC1234","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.licensePlate").value("ABC1234"))
                .andExpect(jsonPath("$.hasViolation").value(true));
    }

    @Test
    @DisplayName("POST: should return 400 when license plate is invalid")
    void invalidLicensePlate() throws Exception {
        String body = """
                {"licensePlate":"INVALID","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST: should return 400 when captureTimestamp is in the future")
    void futureTimestamp() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2099-01-01T00:00:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST: should return 400 when x-origin is missing")
    void missingOrigin() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST: should return 400 when x-origin is invalid")
    void invalidOrigin() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":50,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "INVALID")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST: should return 400 when request body is empty")
    void emptyBody() throws Exception {
        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST: should return 400 when measuredSpeed is negative")
    void negativeSpeed() throws Exception {
        String body = """
                {"licensePlate":"ABC1D23","measuredSpeed":-10,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                """;

        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET: should return 200 with previously created violations")
    void getViolationsAfterPost() throws Exception {
        mockMvc.perform(post("/api/v1/violations/evaluate")
                        .header("x-origin", "FIXED")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"licensePlate":"GET1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-CWB-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/violations")
                        .param("licensePlate", "GET1D23"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].licensePlate").value("GET1D23"))
                .andExpect(jsonPath("$[0].hasViolation").value(true))
                .andExpect(jsonPath("$[0].violation.severity").value("SERIOUS"));
    }

    @Test
    @DisplayName("GET: should return 200 with empty list when no violations exist")
    void noViolations() throws Exception {
        mockMvc.perform(get("/api/v1/violations")
                        .param("licensePlate", "ZZZ9Z99"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("GET: should return 400 when licensePlate param is missing")
    void missingParam() throws Exception {
        mockMvc.perform(get("/api/v1/violations"))
                .andExpect(status().isBadRequest());
    }
}
