package com.jadecaravan.adapter.in.web;

import com.jadecaravan.adapter.in.web.dto.ApplicationStatusResponse;
import com.jadecaravan.application.model.ApplicationStatus;
import com.jadecaravan.application.port.in.ApplicationStatusQuery;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/status", produces = MediaType.APPLICATION_JSON_VALUE)
public class ApplicationStatusController {

    private final ApplicationStatusQuery applicationStatusQuery;

    public ApplicationStatusController(ApplicationStatusQuery applicationStatusQuery) {
        this.applicationStatusQuery = applicationStatusQuery;
    }

    @GetMapping
    public ApplicationStatusResponse status() {
        ApplicationStatus applicationStatus = applicationStatusQuery.execute();
        return new ApplicationStatusResponse(
                applicationStatus.ready(),
                applicationStatus.metadata().applicationName(),
                applicationStatus.metadata().activeProfile(),
                applicationStatus.metadata().version());
    }
}
