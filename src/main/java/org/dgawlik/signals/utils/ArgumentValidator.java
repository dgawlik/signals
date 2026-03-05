package org.dgawlik.signals.utils;

import java.util.List;

public enum ArgumentValidator {
    VAL();

    public ArgumentValidator requireNonEmpty(List<?> list) {
        if (list == null || list.isEmpty()) {
            throw new IllegalArgumentException("List cannot be null or empty");
        }
        return this;
    }

    public ArgumentValidator requireNonNull(Object o) {
        if (o == null) {
            throw new IllegalArgumentException("Object cannot be null");
        }
        return this;
    }

    public ArgumentValidator requireNonNegative(double d) {
        if (d < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
        return this;
    }

    public ArgumentValidator requireNonNegative(int i) {
        if (i < 0) {
            throw new IllegalArgumentException("Value cannot be negative");
        }
        return this;
    }

    public ArgumentValidator requireNotBlank(String s) {
        if (s == null || s.isBlank()) {
            throw new IllegalArgumentException("String cannot be null or blank");
        }
        return this;
    }

    public ArgumentValidator requireDoesNotContainNull(List<?> list) {
        if (list == null || list.stream().anyMatch(o -> o == null)) {
            throw new IllegalArgumentException("List cannot contain null values");
        }
        return this;
    }

}
