package br.com.velsis.SpeedViolationService.store;

import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse.ViolationDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationStoreTest {

    private ViolationStore store;

    @BeforeEach
    void setUp() {
        store = new ViolationStore();
    }

    @Nested
    @DisplayName("Save and find")
    class SaveAndFind {

        @Test
        @DisplayName("should return empty list when no violations saved")
        void noViolations() {
            List<ViolationResponse> result = store.findByLicensePlate("ABC1D23");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return saved violation for license plate")
        void singleViolation() {
            ViolationResponse violation = aViolation("ABC1D23");

            store.save(violation);
            List<ViolationResponse> result = store.findByLicensePlate("ABC1D23");

            assertThat(result).containsExactly(violation);
        }

        @Test
        @DisplayName("should return multiple violations for same plate in order")
        void multipleViolationsSamePlate() {
            ViolationResponse v1 = aViolation("ABC1D23");
            ViolationResponse v2 = aViolation("ABC1D23");

            store.save(v1);
            store.save(v2);
            List<ViolationResponse> result = store.findByLicensePlate("ABC1D23");

            assertThat(result).containsExactly(v1, v2);
        }

        @Test
        @DisplayName("should not return violations for other plates")
        void differentPlates() {
            store.save(aViolation("ABC1D23"));
            store.save(aViolation("XYZ9A88"));

            List<ViolationResponse> result = store.findByLicensePlate("OTHER");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return violations only for the requested plate")
        void returnsOnlyRequestedPlate() {
            store.save(aViolation("ABC1D23"));
            store.save(aViolation("XYZ9A88"));

            List<ViolationResponse> result = store.findByLicensePlate("ABC1D23");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().licensePlate()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("should return immutable list")
        void returnsImmutableList() {
            store.save(aViolation("ABC1D23"));
            List<ViolationResponse> result = store.findByLicensePlate("ABC1D23");

            org.junit.jupiter.api.Assertions.assertThrows(
                    UnsupportedOperationException.class,
                    () -> result.add(aViolation("ABC1D23"))
            );
        }
    }

    private static ViolationResponse aViolation(String licensePlate) {
        return new ViolationResponse(
                licensePlate, "RAD-001", 100, 93, 80, 16.25, true,
                new ViolationDetails("MEDIUM", "218-I"), OffsetDateTime.now()
        );
    }
}
