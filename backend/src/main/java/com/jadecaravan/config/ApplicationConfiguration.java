package com.jadecaravan.config;

import com.jadecaravan.adapter.out.system.EnvironmentApplicationMetadataAdapter;
import com.jadecaravan.application.port.in.ApplicationStatusQuery;
import com.jadecaravan.application.port.out.ApplicationMetadataPort;
import com.jadecaravan.application.service.ApplicationStatusService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class ApplicationConfiguration {

    @Bean
    ApplicationMetadataPort applicationMetadataPort(Environment environment) {
        return new EnvironmentApplicationMetadataAdapter(environment);
    }

    @Bean
    ApplicationStatusQuery applicationStatusQuery(ApplicationMetadataPort applicationMetadataPort) {
        return new ApplicationStatusService(applicationMetadataPort);
    }
}
