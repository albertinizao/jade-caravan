package com.jadecaravan.adapter.out.system;

import com.jadecaravan.application.model.ApplicationMetadata;
import com.jadecaravan.application.port.out.ApplicationMetadataPort;
import java.util.Arrays;
import org.springframework.core.env.Environment;

public class EnvironmentApplicationMetadataAdapter implements ApplicationMetadataPort {

    private final Environment environment;

    public EnvironmentApplicationMetadataAdapter(Environment environment) {
        this.environment = environment;
    }

    @Override
    public ApplicationMetadata currentMetadata() {
        String applicationName = environment.getProperty("spring.application.name", "jade-caravan-backend");
        String version = environment.getProperty("application.version", "0.1.0-SNAPSHOT");
        String activeProfile = Arrays.stream(environment.getActiveProfiles())
                .findFirst()
                .orElse("local");

        return new ApplicationMetadata(applicationName, activeProfile, version);
    }
}
