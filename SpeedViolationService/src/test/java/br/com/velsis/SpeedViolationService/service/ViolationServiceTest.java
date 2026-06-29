package br.com.velsis.SpeedViolationService.service;

import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationServiceTest {

    private ViolationService service;

    @BeforeEach
    void setUp() {
        service = new ViolationService();
    }

    @Nested
    @DisplayName("No violation cases")
    class NoViolation {

        @Test
        @DisplayName("should return hasViolation false when speed is below limit")
        void speedBelowLimit() {
            var request = new CaptureRequestDTO("ABC1D23", 50, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.excessPercentage()).isZero();
            assertThat(response.violation()).isNull();
            assertThat(response.consideredSpeed()).isEqualTo(43.0);
        }

        @Test
        @DisplayName("should return hasViolation false when speed equals limit minus tolerance")
        void speedEqualToLimitMinusTolerance() {
            var request = new CaptureRequestDTO("ABC1D23", 67, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.consideredSpeed()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("should return hasViolation false when considered speed equals speed limit")
        void speedEqualToLimitAfterTolerance() {
            var request = new CaptureRequestDTO("ABC1D23", 67, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should handle zero measured speed")
        void zeroSpeed() {
            var request = new CaptureRequestDTO("ABC1D23", 0, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.measuredSpeed()).isZero();
            assertThat(response.consideredSpeed()).isZero();
        }
    }

    @Nested
    @DisplayName("Violation cases")
    class WithViolation {

        @Test
        @DisplayName("should classify MEDIUM when excess is up to 20%")
        void mediumViolation() {
            var request = new CaptureRequestDTO("ABC1D23", 78, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(71.0);
            assertThat(response.excessPercentage()).isEqualTo(18.33);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
            assertThat(response.violation().ctbCode()).isEqualTo("218-I");
        }

        @Test
        @DisplayName("should classify SERIOUS when excess is between 20%% and 50%%")
        void seriousViolation() {
            var request = new CaptureRequestDTO("ABC1D23", 92, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(85.0);
            assertThat(response.excessPercentage()).isEqualTo(41.67);
            assertThat(response.violation().severity()).isEqualTo("SERIOUS");
            assertThat(response.violation().ctbCode()).isEqualTo("218-II");
        }

        @Test
        @DisplayName("should classify GRAVE when excess exceeds 50%")
        void verySeriousViolation() {
            var request = new CaptureRequestDTO("ABC1D23", 120, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(113.0);
            assertThat(response.excessPercentage()).isEqualTo(88.33);
            assertThat(response.violation().severity()).isEqualTo("GRAVE");
            assertThat(response.violation().ctbCode()).isEqualTo("218-III");
        }

        @Test
        @DisplayName("should classify MEDIUM at exactly 20% excess (CTB Art. 218 I)")
        void excessExactly20Percent() {
            var request = new CaptureRequestDTO("ABC1D23", 79, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(20.0);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
            assertThat(response.violation().ctbCode()).isEqualTo("218-I");
        }

        @Test
        @DisplayName("should classify SERIOUS at exactly 50% excess (CTB Art. 218 II)")
        void excessExactly50Percent() {
            var request = new CaptureRequestDTO("ABC1D23", 97, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(50.0);
            assertThat(response.violation().severity()).isEqualTo("SERIOUS");
            assertThat(response.violation().ctbCode()).isEqualTo("218-II");
        }
    }

    @Nested
    @DisplayName("Response fields")
    class ResponseFields {

        @Test
        @DisplayName("should include license plate and equipment id from request")
        void preservesRequestFields() {
            var request = new CaptureRequestDTO("XYZ9A88", 100, 80, "RAD-SP-042", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.licensePlate()).isEqualTo("XYZ9A88");
            assertThat(response.equipmentId()).isEqualTo("RAD-SP-042");
            assertThat(response.measuredSpeed()).isEqualTo(100.0);
            assertThat(response.speedLimit()).isEqualTo(80.0);
        }

        @Test
        @DisplayName("should set processedAt to current timestamp")
        void processedAtIsPresent() {
            var request = new CaptureRequestDTO("ABC1D23", 92, 60, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.processedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Parameterized boundary tests")
    class Boundaries {

        @ParameterizedTest
        @CsvSource({
                "60, 60, 53, false",
                "61, 60, 54, false",
                "67, 60, 60, false",
                "68, 60, 61, true",
                "200, 60, 193, true"
        })
        @DisplayName("should correctly determine violation at speed boundaries")
        void violationBoundaries(double measured, double limit, double expectedConsidered, boolean expectedViolation) {
            var request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(expectedConsidered);
            assertThat(response.hasViolation()).isEqualTo(expectedViolation);
        }
    }

    @Nested
    @DisplayName("Tolerance threshold at 100 km/h")
    class ToleranceThreshold {

        @ParameterizedTest
        @CsvSource({
                "100, 80, 93, true",
                "101, 80, 94, true",
                "100, 95, 93, false",
                "101, 95, 94, false"
        })
        @DisplayName("should apply 7 km/h tolerance at or below 100 km/h and 7% above")
        void speed100Threshold(double measured, double limit, double expectedConsidered, boolean expectedViolation) {
            var request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(expectedConsidered);
            assertThat(response.hasViolation()).isEqualTo(expectedViolation);
        }
    }

    @Nested
    @DisplayName("Severity classification")
    class SeverityClassification {

        @ParameterizedTest
        @CsvSource({
                "78, 60, 71, 18.33, MEDIUM, 218-I",
                "92, 60, 85, 41.67, SERIOUS, 218-II",
                "120, 60, 113, 88.33, GRAVE, 218-III"
        })
        @DisplayName("should classify severity correctly across all CTB levels")
        void allSeverities(double measured, double limit, double expectedConsidered,
                           double expectedExcess, String expectedSeverity, String expectedCtb) {
            var request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", "2026-06-08T14:30:00Z");

            var response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(expectedConsidered);
            assertThat(response.excessPercentage()).isEqualTo(expectedExcess);
            assertThat(response.violation().severity()).isEqualTo(expectedSeverity);
            assertThat(response.violation().ctbCode()).isEqualTo(expectedCtb);
        }
    }
}
