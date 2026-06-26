package com.jadecaravan.application.model;

public record ApplicationMetadata(
        String applicationName,
        String activeProfile,
        String version) {
}
