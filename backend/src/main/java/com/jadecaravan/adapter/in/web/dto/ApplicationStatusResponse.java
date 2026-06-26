package com.jadecaravan.adapter.in.web.dto;

public record ApplicationStatusResponse(
        boolean ready,
        String applicationName,
        String activeProfile,
        String version) {
}
