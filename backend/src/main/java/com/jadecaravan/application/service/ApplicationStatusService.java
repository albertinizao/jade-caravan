package com.jadecaravan.application.service;

import com.jadecaravan.application.model.ApplicationStatus;
import com.jadecaravan.application.port.in.ApplicationStatusQuery;
import com.jadecaravan.application.port.out.ApplicationMetadataPort;

public class ApplicationStatusService implements ApplicationStatusQuery {

    private final ApplicationMetadataPort applicationMetadataPort;

    public ApplicationStatusService(ApplicationMetadataPort applicationMetadataPort) {
        this.applicationMetadataPort = applicationMetadataPort;
    }

    @Override
    public ApplicationStatus execute() {
        return new ApplicationStatus(true, applicationMetadataPort.currentMetadata());
    }
}
