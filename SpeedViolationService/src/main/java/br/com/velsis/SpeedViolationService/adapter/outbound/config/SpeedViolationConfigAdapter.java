package br.com.velsis.SpeedViolationService.adapter.outbound.config;

import br.com.velsis.SpeedViolationService.config.SpeedViolationProperties;
import br.com.velsis.SpeedViolationService.domain.port.outbound.ViolationConfigPort;
import org.springframework.stereotype.Component;

@Component
public class SpeedViolationConfigAdapter implements ViolationConfigPort {

    private final SpeedViolationProperties properties;

    public SpeedViolationConfigAdapter(SpeedViolationProperties properties) {
        this.properties = properties;
    }

    @Override
    public double getToleranceFixed() {
        return properties.getToleranceFixed();
    }

    @Override
    public double getTolerancePercentage() {
        return properties.getTolerancePercentage();
    }

    @Override
    public double getThreshold() {
        return properties.getThreshold();
    }

    @Override
    public double getExcessLimitMedium() {
        return properties.getExcessLimitMedium();
    }

    @Override
    public double getExcessLimitSerious() {
        return properties.getExcessLimitSerious();
    }
}
