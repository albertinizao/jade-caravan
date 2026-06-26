package com.jadecaravan.application.model;

public record ApplicationStatus(
        boolean ready,
        ApplicationMetadata metadata) {
}
