package com.jadecaravan.domain.campaign;

import java.math.BigDecimal;
import java.util.Objects;

final class DomainValidation {

    private DomainValidation() {
    }

    static <T> T requireNonNull(T value, String name) {
        return Objects.requireNonNull(value, name + " must not be null");
    }

    static String requireNonBlank(String value, String name) {
        requireNonNull(value, name);
        String trimmedValue = value.trim();
        if (trimmedValue.isEmpty()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
        return trimmedValue;
    }

    static int requireRangeInclusive(int value, String name, int minInclusive, int maxInclusive) {
        if (value < minInclusive || value > maxInclusive) {
            throw new IllegalArgumentException(
                    name + " must be between " + minInclusive + " and " + maxInclusive + " inclusive");
        }
        return value;
    }

    static long requireNonNegative(long value, String name) {
        if (value < 0L) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    static BigDecimal requireNonNegative(BigDecimal value, String name) {
        requireNonNull(value, name);
        if (value.signum() < 0) {
            throw new IllegalArgumentException(name + " must not be negative");
        }
        return value;
    }

    static BigDecimal requireRangeInclusive(
            BigDecimal value,
            String name,
            BigDecimal minInclusive,
            BigDecimal maxInclusive) {
        requireNonNull(value, name);
        requireNonNull(minInclusive, "minInclusive");
        requireNonNull(maxInclusive, "maxInclusive");
        if (value.compareTo(minInclusive) < 0 || value.compareTo(maxInclusive) > 0) {
            throw new IllegalArgumentException(
                    name + " must be between " + minInclusive + " and " + maxInclusive + " inclusive");
        }
        return value;
    }
}
