package com.jadecaravan.application.catalog.service;

public class CatalogNotFoundException extends RuntimeException {

    private final String catalogName;

    public CatalogNotFoundException(String catalogName, Throwable cause) {
        super("Unknown catalog: " + catalogName, cause);
        this.catalogName = catalogName;
    }

    public String catalogName() {
        return catalogName;
    }
}
