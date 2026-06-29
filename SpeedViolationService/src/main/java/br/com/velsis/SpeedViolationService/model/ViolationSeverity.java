package br.com.velsis.SpeedViolationService.model;

public enum ViolationSeverity {
    MEDIUM("218-I"),
    SERIOUS("218-II"),
    GRAVE("218-III");

    private final String ctbCode;

    ViolationSeverity(String ctbCode) {
        this.ctbCode = ctbCode;
    }

    public String ctbCode() {
        return ctbCode;
    }
}
