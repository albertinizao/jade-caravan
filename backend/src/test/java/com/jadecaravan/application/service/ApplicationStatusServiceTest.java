package com.jadecaravan.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.jadecaravan.application.model.ApplicationMetadata;
import com.jadecaravan.application.model.ApplicationStatus;
import com.jadecaravan.application.port.out.ApplicationMetadataPort;
import org.junit.jupiter.api.Test;

class ApplicationStatusServiceTest {

    @Test
    void returnsReadyStatusWithMetadata() {
        ApplicationMetadataPort metadataPort = () -> new ApplicationMetadata("jade-caravan-backend", "test", "0.1.0-SNAPSHOT");
        ApplicationStatusService service = new ApplicationStatusService(metadataPort);

        ApplicationStatus status = service.execute();

        assertThat(status.ready()).isTrue();
        assertThat(status.metadata().applicationName()).isEqualTo("jade-caravan-backend");
        assertThat(status.metadata().activeProfile()).isEqualTo("test");
        assertThat(status.metadata().version()).isEqualTo("0.1.0-SNAPSHOT");
    }
}
