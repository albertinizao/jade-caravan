package com.jadecaravan.config;

import com.jadecaravan.application.catalog.port.in.CatalogQueryUseCase;
import com.jadecaravan.application.catalog.service.CatalogQueryApplicationService;
import com.jadecaravan.adapter.out.system.EnvironmentApplicationMetadataAdapter;
import com.jadecaravan.domain.catalog.CatalogRegistry;
import com.jadecaravan.application.campaign.port.in.CampaignRulesUseCase;
import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.application.campaign.port.out.CampaignDailyCycleRepository;
import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.application.campaign.service.CampaignDailyCycleApplicationService;
import com.jadecaravan.application.campaign.service.CampaignRulesApplicationService;
import com.jadecaravan.application.port.in.ApplicationStatusQuery;
import com.jadecaravan.application.port.out.ApplicationMetadataPort;
import com.jadecaravan.application.service.ApplicationStatusService;
import com.jadecaravan.domain.calculation.CaravanCalculationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import java.time.Clock;

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
    CaravanCalculationService caravanCalculationService() {
        return new CaravanCalculationService();
    }

    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    com.jadecaravan.application.campaign.port.in.CampaignDailyCycleUseCase campaignDailyCycleUseCase(
            CampaignDailyCycleRepository campaignDailyCycleRepository,
            CampaignRulesRepository campaignRulesRepository,
            CampaignAuditRepository campaignAuditRepository,
            CatalogRegistry catalogRegistry,
            CaravanCalculationService caravanCalculationService,
            Clock clock) {
        return new CampaignDailyCycleApplicationService(
                campaignDailyCycleRepository,
                campaignRulesRepository,
                campaignAuditRepository,
                catalogRegistry,
                caravanCalculationService,
                clock);
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
