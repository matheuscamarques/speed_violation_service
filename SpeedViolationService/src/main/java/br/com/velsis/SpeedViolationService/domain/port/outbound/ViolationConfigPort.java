package br.com.velsis.SpeedViolationService.domain.port.outbound;

public interface ViolationConfigPort {
    double getToleranceFixed();
    double getTolerancePercentage();
    double getThreshold();
    double getExcessLimitMedium();
    double getExcessLimitSerious();
}
