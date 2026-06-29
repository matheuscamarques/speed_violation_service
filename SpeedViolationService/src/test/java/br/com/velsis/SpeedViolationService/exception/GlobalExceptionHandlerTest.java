package br.com.velsis.SpeedViolationService.exception;

import br.com.velsis.SpeedViolationService.adapter.inbound.web.ViolationEvaluateController;
import br.com.velsis.SpeedViolationService.domain.port.inbound.ViolationEvaluationUseCase;
import br.com.velsis.SpeedViolationService.dto.ErrorResponse;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;
    private ViolationEvaluationUseCase violationEvaluationUseCase;

    @BeforeEach
    void setUp() {
        violationEvaluationUseCase = mock(ViolationEvaluationUseCase.class);
        mockMvc = MockMvcBuilders.standaloneSetup(
                        new ViolationEvaluateController(violationEvaluationUseCase))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("Validation errors (4xx)")
    class ValidationErrors {

        @Test
        @DisplayName("should return 400 with ErrorResponse for invalid request body")
        void invalidBody() throws Exception {
            String body = """
                    {"licensePlate":"","measuredSpeed":-1,"speedLimit":0,"equipmentId":"","captureTimestamp":""}
                    """;

            mockMvc.perform(post("/api/v1/violations/evaluate")
                            .header("x-origin", "FIXED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Validation failed"))
                    .andExpect(jsonPath("$.path").value("/api/v1/violations/evaluate"))
                    .andExpect(jsonPath("$.timestamp").isNotEmpty());
        }

        @Test
        @DisplayName("should return 400 with ErrorResponse for missing header")
        void missingHeader() throws Exception {
            String body = """
                    {"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                    """;

            mockMvc.perform(post("/api/v1/violations/evaluate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Missing required header"))
                    .andExpect(jsonPath("$.message").value("x-origin"));
        }

        @Test
        @DisplayName("should return 400 with ErrorResponse for missing query param")
        void missingQueryParam() throws Exception {
            mockMvc.perform(get("/api/v1/violations"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Missing required parameter"));
        }

        @Test
        @DisplayName("should return 400 with ErrorResponse for malformed JSON")
        void malformedJson() throws Exception {
            mockMvc.perform(post("/api/v1/violations/evaluate")
                            .header("x-origin", "FIXED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{invalid}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.error").value("Malformed request body"));
        }
    }

    @Nested
    @DisplayName("Unexpected errors (5xx)")
    class UnexpectedErrors {

        @Test
        @DisplayName("should return 500 with ErrorResponse and no stack trace")
        void internalError() throws Exception {
            when(violationEvaluationUseCase.evaluate(any())).thenThrow(new RuntimeException("connection refused"));

            String body = """
                    {"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                    """;

            mockMvc.perform(post("/api/v1/violations/evaluate")
                            .header("x-origin", "FIXED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.status").value(500))
                    .andExpect(jsonPath("$.error").value("Internal server error"))
                    .andExpect(jsonPath("$.message").doesNotExist())
                    .andExpect(jsonPath("$.path").value("/api/v1/violations/evaluate"));
        }
    }

    @Nested
    @DisplayName("Valid requests still work")
    class ValidRequests {

        @Test
        @DisplayName("should return 200 without triggering error handler")
        void validPost() throws Exception {
            ViolationResponse response = new ViolationResponse("ABC1D23", "RAD-001", 92, 85, 60, 41.67, true,
                    new ViolationResponse.ViolationDetails("SERIOUS", "218-II"), java.time.OffsetDateTime.now());
            when(violationEvaluationUseCase.evaluate(any())).thenReturn(response);

            String body = """
                    {"licensePlate":"ABC1D23","measuredSpeed":92,"speedLimit":60,"equipmentId":"RAD-001","captureTimestamp":"2026-06-08T14:30:00Z"}
                    """;

            mockMvc.perform(post("/api/v1/violations/evaluate")
                            .header("x-origin", "FIXED")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.licensePlate").value("ABC1D23"));
        }
    }
}
