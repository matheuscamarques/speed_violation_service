package br.com.velsis.SpeedViolationService.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "speed.violation")
public class SpeedViolationProperties {

    private double toleranceFixed = 7.0;
    private double tolerancePercentage = 7.0;
    private double threshold = 100.0;
    private double excessLimitMedium = 20.0;
    private double excessLimitSerious = 50.0;

    public double getToleranceFixed() {
        return toleranceFixed;
    }

    public void setToleranceFixed(double toleranceFixed) {
        this.toleranceFixed = toleranceFixed;
    }

    public double getTolerancePercentage() {
        return tolerancePercentage;
    }

    public void setTolerancePercentage(double tolerancePercentage) {
        this.tolerancePercentage = tolerancePercentage;
    }

    public double getThreshold() {
        return threshold;
    }

    public void setThreshold(double threshold) {
        this.threshold = threshold;
    }

    public double getExcessLimitMedium() {
        return excessLimitMedium;
    }

    public void setExcessLimitMedium(double excessLimitMedium) {
        this.excessLimitMedium = excessLimitMedium;
    }

    public double getExcessLimitSerious() {
        return excessLimitSerious;
    }

    public void setExcessLimitSerious(double excessLimitSerious) {
        this.excessLimitSerious = excessLimitSerious;
    }
}
