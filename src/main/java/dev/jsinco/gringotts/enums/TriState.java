package dev.jsinco.gringotts.enums;

public enum TriState {
    TRUE, FALSE, ALTERNATIVE_STATE;

    public boolean toBoolean() {
        return this == TRUE;
    }
}
