package br.com.velsis.SpeedViolationService.adapter.outbound.persistence;

import br.com.velsis.SpeedViolationService.domain.model.Violation;
import br.com.velsis.SpeedViolationService.domain.model.ViolationSeverity;
import jakarta.persistence.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "violations")
public class ViolationEntity {

    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(nullable = false, length = 10)
    private String licensePlate;

    @Column(nullable = false, length = 20)
    private String equipmentId;

    @Column(nullable = false)
    private double measuredSpeed;

    @Column(nullable = false)
    private double consideredSpeed;

    @Column(nullable = false)
    private double speedLimit;

    @Column(nullable = false)
    private double excessPercentage;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ViolationSeverity severity;

    @Column(nullable = false, length = 10)
    private String ctbCode;

    @Column(nullable = false)
    private OffsetDateTime captureTimestamp;

    @Column(nullable = false)
    private OffsetDateTime processedAt;

    @Deprecated
    protected ViolationEntity() {
    }

    public ViolationEntity(Violation violation) {
        this.id = violation.id();
        this.licensePlate = violation.licensePlate();
        this.equipmentId = violation.equipmentId();
        this.measuredSpeed = violation.measuredSpeed();
        this.consideredSpeed = violation.consideredSpeed();
        this.speedLimit = violation.speedLimit();
        this.excessPercentage = violation.excessPercentage();
        this.severity = violation.severity();
        this.ctbCode = violation.ctbCode();
        this.captureTimestamp = violation.captureTimestamp();
        this.processedAt = violation.processedAt();
    }

    public Violation toDomain() {
        return new Violation(
                id, licensePlate, equipmentId, measuredSpeed, consideredSpeed,
                speedLimit, excessPercentage, severity, ctbCode,
                captureTimestamp, processedAt
        );
    }

    public UUID getId() {
        return id;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public String getEquipmentId() {
        return equipmentId;
    }

    public double getMeasuredSpeed() {
        return measuredSpeed;
    }

    public double getConsideredSpeed() {
        return consideredSpeed;
    }

    public double getSpeedLimit() {
        return speedLimit;
    }

    public double getExcessPercentage() {
        return excessPercentage;
    }

    public ViolationSeverity getSeverity() {
        return severity;
    }

    public String getCtbCode() {
        return ctbCode;
    }

    public OffsetDateTime getCaptureTimestamp() {
        return captureTimestamp;
    }

    public OffsetDateTime getProcessedAt() {
        return processedAt;
    }
}
