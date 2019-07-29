package com.monjam.core.api;

import java.util.Arrays;

public enum MigrationType {
    MIGRATE("V"), ROLLBACK("U");

    private String value;

    MigrationType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MigrationType fromString(String value) {
        return Arrays.stream(MigrationType.values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElse(null);
    }
}
