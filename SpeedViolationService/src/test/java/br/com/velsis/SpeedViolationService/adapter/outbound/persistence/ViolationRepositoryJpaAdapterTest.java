package br.com.velsis.SpeedViolationService.adapter.outbound.persistence;

import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.model.ViolationSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ViolationRepositoryJpaAdapterTest {

    private ViolationJpaRepository jpaRepository;
    private ViolationRepositoryJpaAdapter adapter;

    @BeforeEach
    void setUp() {
        jpaRepository = mock(ViolationJpaRepository.class);
        adapter = new ViolationRepositoryJpaAdapter(jpaRepository);
    }

    @Nested
    @DisplayName("Save violation")
    class Save {

        @Test
        @DisplayName("should convert to entity and delegate to JPA repository")
        void save() {
            Violation violation = aViolation("ABC1D23");

            adapter.save(violation);

            ArgumentCaptor<ViolationEntity> captor = ArgumentCaptor.forClass(ViolationEntity.class);
            verify(jpaRepository).save(captor.capture());
            ViolationEntity entity = captor.getValue();
            assertThat(entity.getLicensePlate()).isEqualTo("ABC1D23");
            assertThat(entity.getMeasuredSpeed()).isEqualTo(100);
        }
    }

    @Nested
    @DisplayName("Find by license plate")
    class FindByLicensePlate {

        @Test
        @DisplayName("should return empty list when no entities found")
        void noViolations() {
            when(jpaRepository.findByLicensePlate("ABC1D23")).thenReturn(List.of());

            List<Violation> result = adapter.findByLicensePlate("ABC1D23");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should convert entities to domain objects")
        void withViolations() {
            OffsetDateTime captureTimestamp = OffsetDateTime.parse("2026-06-08T14:30:00Z");
            OffsetDateTime processedAt = OffsetDateTime.parse("2026-06-08T14:31:00Z");
            UUID id = UUID.randomUUID();
            Violation violation = new Violation(
                    id, "ABC1D23", "RAD-001", 92, 85, 60, 41.67,
                    ViolationSeverity.SERIOUS, "218-II",
                    captureTimestamp, processedAt
            );
            ViolationEntity entity = new ViolationEntity(violation);

            when(jpaRepository.findByLicensePlate("ABC1D23")).thenReturn(List.of(entity));

            List<Violation> result = adapter.findByLicensePlate("ABC1D23");

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().id()).isEqualTo(id);
            assertThat(result.getFirst().licensePlate()).isEqualTo("ABC1D23");
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
