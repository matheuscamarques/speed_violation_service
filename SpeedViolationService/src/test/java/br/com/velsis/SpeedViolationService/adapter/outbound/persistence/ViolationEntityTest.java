package br.com.velsis.SpeedViolationService.adapter.outbound.persistence;

import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.model.ViolationSeverity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ViolationEntityTest {

    @Nested
    @DisplayName("Construction from Violation domain object")
    class FromDomain {

        @Test
        @DisplayName("should create entity with all fields from domain violation")
        void fromViolation() {
            OffsetDateTime captureTimestamp = OffsetDateTime.parse("2026-06-08T14:30:00Z");
            OffsetDateTime processedAt = OffsetDateTime.parse("2026-06-08T14:31:00Z");
            UUID id = UUID.randomUUID();
            Violation violation = new Violation(
                    id, "ABC1D23", "RAD-001", 92, 85, 60, 41.67,
                    ViolationSeverity.SERIOUS, "218-II",
                    captureTimestamp, processedAt
            );

            ViolationEntity entity = new ViolationEntity(violation);

            assertThat(entity.getId()).isEqualTo(id);
            assertThat(entity.getLicensePlate()).isEqualTo("ABC1D23");
            assertThat(entity.getEquipmentId()).isEqualTo("RAD-001");
            assertThat(entity.getMeasuredSpeed()).isEqualTo(92);
            assertThat(entity.getConsideredSpeed()).isEqualTo(85);
            assertThat(entity.getSpeedLimit()).isEqualTo(60);
            assertThat(entity.getExcessPercentage()).isEqualTo(41.67);
            assertThat(entity.getSeverity()).isEqualTo(ViolationSeverity.SERIOUS);
            assertThat(entity.getCtbCode()).isEqualTo("218-II");
            assertThat(entity.getCaptureTimestamp()).isEqualTo(captureTimestamp);
            assertThat(entity.getProcessedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("Conversion to domain object")
    class ToDomain {

        @Test
        @DisplayName("should convert entity back to violation domain object")
        void toDomain() {
            OffsetDateTime captureTimestamp = OffsetDateTime.parse("2026-06-08T14:30:00Z");
            OffsetDateTime processedAt = OffsetDateTime.parse("2026-06-08T14:31:00Z");
            UUID id = UUID.randomUUID();
            Violation violation = new Violation(
                    id, "ABC1D23", "RAD-001", 92, 85, 60, 41.67,
                    ViolationSeverity.SERIOUS, "218-II",
                    captureTimestamp, processedAt
            );

            ViolationEntity entity = new ViolationEntity(violation);
            Violation result = entity.toDomain();

            assertThat(result.id()).isEqualTo(id);
            assertThat(result.licensePlate()).isEqualTo("ABC1D23");
            assertThat(result.equipmentId()).isEqualTo("RAD-001");
            assertThat(result.measuredSpeed()).isEqualTo(92);
            assertThat(result.consideredSpeed()).isEqualTo(85);
            assertThat(result.speedLimit()).isEqualTo(60);
            assertThat(result.excessPercentage()).isEqualTo(41.67);
            assertThat(result.severity()).isEqualTo(ViolationSeverity.SERIOUS);
            assertThat(result.ctbCode()).isEqualTo("218-II");
            assertThat(result.captureTimestamp()).isEqualTo(captureTimestamp);
            assertThat(result.processedAt()).isEqualTo(processedAt);
        }
    }

    @Nested
    @DisplayName("JPA no-arg constructor")
    class JpaConstructor {

        @Test
        @DisplayName("should create entity with no-arg constructor")
        void noArgConstructor() {
            ViolationEntity entity = new ViolationEntity();
            assertThat(entity).isNotNull();
        }
    }
}
