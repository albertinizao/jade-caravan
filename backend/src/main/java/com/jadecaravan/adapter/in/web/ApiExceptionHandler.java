package com.jadecaravan.adapter.in.web;

import com.jadecaravan.application.catalog.service.CatalogNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(CatalogNotFoundException.class)
    public ProblemDetail handleCatalogNotFound(CatalogNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Catálogo no encontrado: " + ex.catalogName());
        problemDetail.setTitle("Catálogo desconocido");
        problemDetail.setType(java.net.URI.create("https://caravan.local/problems/catalog-not-found"));
        problemDetail.setProperty("violations", List.of(
                java.util.Map.of(
                        "field", "catalogName",
                        "code", "CATALOG_NOT_FOUND")));
        return problemDetail;
    }
}
