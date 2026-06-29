package br.com.velsis.SpeedViolationService.service;

import br.com.velsis.SpeedViolationService.adapter.outbound.config.SpeedViolationConfigAdapter;
import br.com.velsis.SpeedViolationService.config.SpeedViolationProperties;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationRepository;
import br.com.velsis.SpeedViolationService.domain.service.ViolationEvaluationService;
import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ViolationServiceTest {

    private ViolationEvaluationService service;
    private ViolationRepository violationRepository;
    private SpeedViolationConfigAdapter config;

    @BeforeEach
    void setUp() {
        violationRepository = mock(ViolationRepository.class);
        SpeedViolationProperties properties = new SpeedViolationProperties();
        config = new SpeedViolationConfigAdapter(properties);
        service = new ViolationEvaluationService(violationRepository, config);
    }

    @Nested
    @DisplayName("No violation cases")
    class NoViolation {

        @Test
        @DisplayName("should return hasViolation false when speed is below limit")
        void speedBelowLimit() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.excessPercentage()).isZero();
            assertThat(response.violation()).isNull();
            assertThat(response.consideredSpeed()).isEqualTo(43.0);
        }

        @Test
        @DisplayName("should return hasViolation false when speed equals limit minus tolerance")
        void speedEqualToLimitMinusTolerance() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 67, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.consideredSpeed()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("should return hasViolation false when considered speed equals speed limit")
        void speedEqualToLimitAfterTolerance() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 67, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should handle zero measured speed")
        void zeroSpeed() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 0, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

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
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 78, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(71.0);
            assertThat(response.excessPercentage()).isEqualTo(18.33);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
            assertThat(response.violation().ctbCode()).isEqualTo("218-I");
        }

        @Test
        @DisplayName("should classify SERIOUS when excess is between 20%% and 50%%")
        void seriousViolation() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 92, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(85.0);
            assertThat(response.excessPercentage()).isEqualTo(41.67);
            assertThat(response.violation().severity()).isEqualTo("SERIOUS");
            assertThat(response.violation().ctbCode()).isEqualTo("218-II");
        }

        @Test
        @DisplayName("should classify VERY_SERIOUS when excess exceeds 50%")
        void verySeriousViolation() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 120, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(113.0);
            assertThat(response.excessPercentage()).isEqualTo(88.33);
            assertThat(response.violation().severity()).isEqualTo("VERY_SERIOUS");
            assertThat(response.violation().ctbCode()).isEqualTo("218-III");
        }

        @Test
        @DisplayName("should classify MEDIUM at exactly 20% excess (CTB Art. 218 I)")
        void excessExactly20Percent() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 79, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(20.0);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
            assertThat(response.violation().ctbCode()).isEqualTo("218-I");
        }

        @Test
        @DisplayName("should classify SERIOUS at exactly 50% excess (CTB Art. 218 II)")
        void excessExactly50Percent() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 97, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

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
            CaptureRequestDTO request = new CaptureRequestDTO("XYZ9A88", 100, 80, "RAD-SP-042", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.licensePlate()).isEqualTo("XYZ9A88");
            assertThat(response.equipmentId()).isEqualTo("RAD-SP-042");
            assertThat(response.measuredSpeed()).isEqualTo(100.0);
            assertThat(response.speedLimit()).isEqualTo(80.0);
        }

        @Test
        @DisplayName("should set processedAt to current timestamp")
        void processedAtIsPresent() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 92, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

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
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

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
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(expectedConsidered);
            assertThat(response.hasViolation()).isEqualTo(expectedViolation);
        }
    }

    @Nested
    @DisplayName("Find by license plate")
    class FindByLicensePlate {

        @Test
        @DisplayName("should return empty list when no violations exist")
        void noViolations() {
            when(violationRepository.findByLicensePlate(anyString())).thenReturn(List.of());

            List<ViolationResponse> result = service.findByLicensePlate("ABC1D23");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return violations when they exist for the plate")
        void withViolations() {
            Violation violation = new Violation(
                    UUID.randomUUID(), "ABC1D23", "RAD-001", 92, 85, 60, 41.67,
                    ViolationSeverity.SERIOUS, "218-II",
                    OffsetDateTime.parse("2026-06-08T14:30:00Z"), OffsetDateTime.now()
            );
            when(violationRepository.findByLicensePlate("ABC1D23")).thenReturn(List.of(violation));

            List<ViolationResponse> result = service.findByLicensePlate("ABC1D23");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().licensePlate()).isEqualTo("ABC1D23");
            assertThat(result.getFirst().violation().severity()).isEqualTo("SERIOUS");
        }
    }

    @Nested
    @DisplayName("Severity classification")
    class SeverityClassification {

        @ParameterizedTest
        @CsvSource({
                "78, 60, 71, 18.33, MEDIUM, 218-I",
                "92, 60, 85, 41.67, SERIOUS, 218-II",
                "120, 60, 113, 88.33, VERY_SERIOUS, 218-III"
        })
        @DisplayName("should classify severity correctly across all CTB levels")
        void allSeverities(double measured, double limit, double expectedConsidered,
                           double expectedExcess, String expectedSeverity, String expectedCtb) {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.consideredSpeed()).isEqualTo(expectedConsidered);
            assertThat(response.excessPercentage()).isEqualTo(expectedExcess);
            assertThat(response.violation().severity()).isEqualTo(expectedSeverity);
            assertThat(response.violation().ctbCode()).isEqualTo(expectedCtb);
        }
    }

    @Nested
    @DisplayName("Excess percentage edge cases")
    class ExcessPercentageEdgeCases {

        @Test
        @DisplayName("should return 0% excess when measured speed equals limit plus tolerance")
        void zeroExcess() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 67, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.excessPercentage()).isZero();
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should round half-up for excess percentage")
        void halfUpRounding() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 80, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(73.0);
            assertThat(response.excessPercentage()).isEqualTo(21.67);
        }

        @Test
        @DisplayName("should handle very large excess values")
        void highExcess() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 300, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(293.0);
            assertThat(response.excessPercentage()).isEqualTo(388.33);
            assertThat(response.violation().severity()).isEqualTo("VERY_SERIOUS");
        }
    }

    @Nested
    @DisplayName("Severity classification edge cases")
    class SeverityEdgeCases {

        @Test
        @DisplayName("should classify MEDIUM for excess just above 0%")
        void mediumJustAboveZero() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 68, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(1.67);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
        }

        @ParameterizedTest
        @CsvSource({
                "79, 60, 20.00, MEDIUM",
                "80, 60, 21.67, SERIOUS",
                "97, 60, 50.00, SERIOUS",
                "98, 60, 51.67, VERY_SERIOUS"
        })
        @DisplayName("should classify severity at exact boundary values")
        void severityBoundaries(double measured, double limit, double expectedExcess, String expectedSeverity) {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", measured, limit, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(expectedExcess);
            assertThat(response.violation().severity()).isEqualTo(expectedSeverity);
        }
    }

    @Nested
    @DisplayName("Custom configuration")
    class CustomConfiguration {

        @Test
        @DisplayName("should use custom fixed tolerance when configured")
        void customFixedTolerance() {
            SpeedViolationProperties properties = new SpeedViolationProperties();
            properties.setToleranceFixed(5.0);
            config = new SpeedViolationConfigAdapter(properties);
            service = new ViolationEvaluationService(violationRepository, config);

            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 64, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(59.0);
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should use custom percentage tolerance when configured")
        void customPercentageTolerance() {
            SpeedViolationProperties properties = new SpeedViolationProperties();
            properties.setTolerancePercentage(10.0);
            properties.setThreshold(49.0);
            config = new SpeedViolationConfigAdapter(properties);
            service = new ViolationEvaluationService(violationRepository, config);

            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 60, 50, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(55.0);
            assertThat(response.excessPercentage()).isEqualTo(10.0);
        }

        @Test
        @DisplayName("should use custom severity limits when configured")
        void customSeverityLimits() {
            SpeedViolationProperties properties = new SpeedViolationProperties();
            properties.setExcessLimitMedium(10.0);
            properties.setExcessLimitSerious(30.0);
            config = new SpeedViolationConfigAdapter(properties);
            service = new ViolationEvaluationService(violationRepository, config);

            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 80, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(21.67);
            assertThat(response.violation().severity()).isEqualTo("SERIOUS");
        }

        @Test
        @DisplayName("should use custom threshold value")
        void customThreshold() {
            SpeedViolationProperties properties = new SpeedViolationProperties();
            properties.setThreshold(79.0);
            properties.setTolerancePercentage(10.0);
            config = new SpeedViolationConfigAdapter(properties);
            service = new ViolationEvaluationService(violationRepository, config);

            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 96, 80, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            double tolerance = 80 * 0.10;
            assertThat(response.consideredSpeed()).isEqualTo(96 - tolerance);
            assertThat(response.hasViolation()).isTrue();
        }
    }

    @Nested
    @DisplayName("RF6.1: Measured speed at or below limit")
    class MeasuredSpeedAtOrBelowLimit {

        @Test
        @DisplayName("should return no violation when measured speed equals limit")
        void speedAtExactlyLimit() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 60, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.consideredSpeed()).isEqualTo(53.0);
            assertThat(response.excessPercentage()).isZero();
            assertThat(response.violation()).isNull();
        }

        @Test
        @DisplayName("should return no violation when measured speed is below limit")
        void speedBelowLimit() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 55, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.consideredSpeed()).isEqualTo(48.0);
        }
    }

    @Nested
    @DisplayName("RF6.2: Speed within tolerance margin")
    class WithinToleranceMargin {

        @Test
        @DisplayName("should return no violation when considered speed equals limit (edge of tolerance)")
        void speedAtExactToleranceEdge() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 67, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.consideredSpeed()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("should return no violation when measured speed minus tolerance is below limit")
        void speedWithinTolerance() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 66, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.hasViolation()).isFalse();
            assertThat(response.consideredSpeed()).isEqualTo(59.0);
        }
    }

    @Nested
    @DisplayName("RF6.3: Both license plate formats")
    class BothLicensePlateFormats {

        @Test
        @DisplayName("should accept old format plate (ABC1234)")
        void oldFormatPlate() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1234", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.licensePlate()).isEqualTo("ABC1234");
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should accept Mercosul format plate (ABC1D23)")
        void mercosulFormatPlate() {
            CaptureRequestDTO request = new CaptureRequestDTO("XYZ9A88", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.licensePlate()).isEqualTo("XYZ9A88");
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should process old format plate through violation flow")
        void oldFormatPlateWithViolation() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1234", 92, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.licensePlate()).isEqualTo("ABC1234");
            assertThat(response.hasViolation()).isTrue();
            assertThat(response.violation().severity()).isEqualTo("SERIOUS");
        }
    }

    @Nested
    @DisplayName("RF6.4: Severity boundary values (exactly 20% and 50%)")
    class SeverityBoundaries {

        @Test
        @DisplayName("should classify MEDIUM at exactly 20% excess (boundary MEDIUM/SERIOUS)")
        void excessExactly20Percent_boundary() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 79, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.excessPercentage()).isEqualTo(20.0);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
            assertThat(response.violation().ctbCode()).isEqualTo("218-I");
        }

        @Test
        @DisplayName("should classify SERIOUS at exactly 50% excess (boundary SERIOUS/VERY_SERIOUS)")
        void excessExactly50Percent_boundary() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 97, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.excessPercentage()).isEqualTo(50.0);
            assertThat(response.violation().severity()).isEqualTo("SERIOUS");
            assertThat(response.violation().ctbCode()).isEqualTo("218-II");
        }
    }

    @Nested
    @DisplayName("RF6.5: Variable tolerance for speed limits above 100 km/h")
    class VariableTolerance {

        @Test
        @DisplayName("should apply 7% tolerance for limit above 100 and return no violation")
        void noViolationWithinPercentageTolerance() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 117, 110, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(109.3);
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should apply 7% tolerance for limit above 100 and detect violation")
        void violationBeyondPercentageTolerance() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 118, 110, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(110.3);
            assertThat(response.hasViolation()).isTrue();
            assertThat(response.excessPercentage()).isEqualTo(0.27);
            assertThat(response.violation().severity()).isEqualTo("MEDIUM");
        }

        @Test
        @DisplayName("should apply fixed 7 km/h tolerance for limit at exactly 100")
        void fixedToleranceAt100() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 107, 100, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(100.0);
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should apply fixed 7 km/h tolerance for limit just below threshold")
        void fixedToleranceBelow100() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 106, 99, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            assertThat(response.consideredSpeed()).isEqualTo(99.0);
            assertThat(response.hasViolation()).isFalse();
        }

        @Test
        @DisplayName("should use percentage tolerance for limit just above 100")
        void percentageToleranceJustAbove100() {
            CaptureRequestDTO request = new CaptureRequestDTO("ABC1D23", 108, 101, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));

            ViolationResponse response = service.evaluate(request);

            double tolerance = 101 * 0.07;
            double expectedConsidered = 108 - tolerance;
            assertThat(response.consideredSpeed()).isEqualTo(expectedConsidered);
            assertThat(response.hasViolation()).isFalse();
        }
    }
}
