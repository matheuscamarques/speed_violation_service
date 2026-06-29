package br.com.velsis.SpeedViolationService.store;

import br.com.velsis.SpeedViolationService.adapter.outbound.persistence.InMemoryViolationRepository;
import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ViolationStoreTest {

    private InMemoryViolationRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryViolationRepository();
    }

    @Nested
    @DisplayName("Save and find")
    class SaveAndFind {

        @Test
        @DisplayName("should return empty list when no violations saved")
        void noViolations() {
            List<Violation> result = repository.findByLicensePlate("ABC1D23");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return saved violation for license plate")
        void singleViolation() {
            Violation violation = aViolation("ABC1D23");

            repository.save(violation);
            List<Violation> result = repository.findByLicensePlate("ABC1D23");

            assertThat(result).containsExactly(violation);
        }

        @Test
        @DisplayName("should return multiple violations for same plate in order")
        void multipleViolationsSamePlate() {
            Violation v1 = aViolation("ABC1D23");
            Violation v2 = aViolation("ABC1D23");

            repository.save(v1);
            repository.save(v2);
            List<Violation> result = repository.findByLicensePlate("ABC1D23");

            assertThat(result).containsExactly(v1, v2);
        }

        @Test
        @DisplayName("should not return violations for other plates")
        void differentPlates() {
            repository.save(aViolation("ABC1D23"));
            repository.save(aViolation("XYZ9A88"));

            List<Violation> result = repository.findByLicensePlate("OTHER");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return violations only for the requested plate")
        void returnsOnlyRequestedPlate() {
            repository.save(aViolation("ABC1D23"));
            repository.save(aViolation("XYZ9A88"));

            List<Violation> result = repository.findByLicensePlate("ABC1D23");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().licensePlate()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("should return immutable list")
        void returnsImmutableList() {
            repository.save(aViolation("ABC1D23"));
            List<Violation> result = repository.findByLicensePlate("ABC1D23");

            assertThrows(
                    UnsupportedOperationException.class,
                    () -> result.add(aViolation("ABC1D23"))
            );
        }
    }

    private static Violation aViolation(String licensePlate) {
        return new Violation(
                UUID.randomUUID(), licensePlate, "RAD-001", 100, 93, 80, 16.25,
                ViolationSeverity.MEDIUM, ViolationSeverity.MEDIUM.ctbCode(),
                OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
