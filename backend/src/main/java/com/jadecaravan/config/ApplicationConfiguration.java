package com.jadecaravan.config;

import com.jadecaravan.application.catalog.port.in.CatalogQueryUseCase;
import com.jadecaravan.application.catalog.service.CatalogQueryApplicationService;
import com.jadecaravan.adapter.out.system.EnvironmentApplicationMetadataAdapter;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.application.campaign.port.in.CampaignRulesUseCase;
import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.application.campaign.service.CampaignRulesApplicationService;
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

    @Bean
    CampaignRulesUseCase campaignRulesUseCase(
            CampaignRulesRepository campaignRulesRepository,
            CampaignAuditRepository campaignAuditRepository) {
        return new CampaignRulesApplicationService(campaignRulesRepository, campaignAuditRepository);
    }

    @Bean
    com.jadecaravan.application.campaign.port.in.CampaignAuditUseCase campaignAuditUseCase(CampaignAuditRepository campaignAuditRepository) {
        return new com.jadecaravan.application.campaign.service.CampaignAuditApplicationService(campaignAuditRepository);
    }

    @Bean
    CatalogRegistry catalogRegistry() {
        return CatalogRegistry.seeded();
    }

    @Bean
    CatalogQueryUseCase catalogQueryUseCase(CatalogRegistry catalogRegistry) {
        return new CatalogQueryApplicationService(catalogRegistry);
    }
}
