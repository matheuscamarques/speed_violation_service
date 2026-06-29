package br.com.velsis.SpeedViolationService.domain.model;

public enum ViolationSeverity {
    MEDIUM("218-I"),
    SERIOUS("218-II"),
    VERY_SERIOUS("218-III");

    private final String ctbCode;

    ViolationSeverity(String ctbCode) {
        this.ctbCode = ctbCode;
    }

    public String ctbCode() {
        return ctbCode;
    }
}
