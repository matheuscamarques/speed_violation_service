package br.com.velsis.SpeedViolationService.service;

import br.com.velsis.SpeedViolationService.dto.CaptureRequestDTO;
import br.com.velsis.SpeedViolationService.dto.ViolationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;

@Service
public class ViolationService {

    private static final double TOLERANCE = 7.0;

    public ViolationResponse evaluate(CaptureRequestDTO request) {
        double measuredSpeed = request.measuredSpeed();
        double consideredSpeed = Math.max(0, measuredSpeed - TOLERANCE);
        double speedLimit = request.speedLimit();
        boolean hasViolation = consideredSpeed > speedLimit;

        double excessPercentage = 0;
        ViolationResponse.ViolationDetails violation = null;

        if (hasViolation) {
            excessPercentage = BigDecimal.valueOf((consideredSpeed - speedLimit) / speedLimit * 100)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();

            violation = classifyViolation(excessPercentage);
        }

        return new ViolationResponse(
                request.licensePlate(),
                request.equipmentId(),
                measuredSpeed,
                consideredSpeed,
                speedLimit,
                excessPercentage,
                hasViolation,
                violation,
                OffsetDateTime.now()
        );
    }

    private ViolationResponse.ViolationDetails classifyViolation(double excessPercentage) {
        if (excessPercentage <= 20) {
            return new ViolationResponse.ViolationDetails("MEDIUM", "218-I");
        } else if (excessPercentage <= 50) {
            return new ViolationResponse.ViolationDetails("SERIOUS", "218-II");
        } else {
            return new ViolationResponse.ViolationDetails("GRAVE", "218-III");
        }
    }
}
