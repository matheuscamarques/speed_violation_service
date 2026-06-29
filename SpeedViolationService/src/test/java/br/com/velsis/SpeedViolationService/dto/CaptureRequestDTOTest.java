package br.com.velsis.SpeedViolationService.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.OffsetDateTime;
import java.util.Set;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

class CaptureRequestDTOTest {

    private static final Pattern PATTERN = Pattern.compile(CaptureRequestDTO.LICENSE_PLATE_REGEX);

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Nested
    @DisplayName("Mercosul format")
    class MercosulFormat {

        @ParameterizedTest
        @ValueSource(strings = {
                "ABC1D23", "XYZ9A88", "BRA0B00", "AAA0A00", "ZZZ9Z99",
                "MER5U23", "SUL7L88", "BRB2B42", "FIM0F00", "MGT5E67"
        })
        @DisplayName("should accept valid Mercosul plates (ABC1D23)")
        void validMercosulPlates(String plate) {
            assertThat(PATTERN.matcher(plate).matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("Old format")
    class OldFormat {

        @ParameterizedTest
        @ValueSource(strings = {
                "ABC1234", "XYZ0000", "BRA9999", "AAA0000", "ZZZ9999",
                "OLD1234", "NEW5678", "FIM0000"
        })
        @DisplayName("should accept valid old format plates (ABC1234)")
        void validOldFormatPlates(String plate) {
            assertThat(PATTERN.matcher(plate).matches()).isTrue();
        }
    }

    @Nested
    @DisplayName("Invalid plates")
    class InvalidPlates {

        @ParameterizedTest
        @ValueSource(strings = {
                "abc1234", "ABC 1234", "ABC-1234", "ABCD123", "ABC12345",
                "AB12345", "ABC1D234", "12A3456", "A1B2C3D", "ABC1D2E",
                "", "A", "AB", "ABC", "ABC1", "ABC12", "ABC123",
                "ABC1D2", "ABC1D", "abc1d23", "Abc1234", "ABCDE12",
                "ÁBC1234"
        })
        @DisplayName("should reject plates with wrong format, length or case")
        void invalidPlates(String plate) {
            assertThat(PATTERN.matcher(plate).matches()).isFalse();
        }
    }

    @Nested
    @DisplayName("Jakarta @Valid annotation")
    class JakartaValidation {

        @Test
        @DisplayName("should pass validation for valid Mercosul plate")
        void validMercosulPlate() {
            var dto = new CaptureRequestDTO("ABC1D23", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));
            Set<ConstraintViolation<CaptureRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
        }

        @Test
        @DisplayName("should pass validation for valid old format plate")
        void validOldFormatPlate() {
            var dto = new CaptureRequestDTO("ABC1234", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));
            Set<ConstraintViolation<CaptureRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).noneMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
        }

        @Test
        @DisplayName("should fail validation for invalid plate")
        void invalidPlate() {
            var dto = new CaptureRequestDTO("INVALID", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));
            Set<ConstraintViolation<CaptureRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
        }

        @Test
        @DisplayName("should fail validation for blank license plate")
        void blankPlate() {
            var dto = new CaptureRequestDTO("", 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));
            Set<ConstraintViolation<CaptureRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
        }

        @Test
        @DisplayName("should fail validation for null license plate")
        void nullPlate() {
            var dto = new CaptureRequestDTO(null, 50, 60, "RAD-001", OffsetDateTime.parse("2026-06-08T14:30:00Z"));
            Set<ConstraintViolation<CaptureRequestDTO>> violations = validator.validate(dto);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("licensePlate"));
        }
    }
}
