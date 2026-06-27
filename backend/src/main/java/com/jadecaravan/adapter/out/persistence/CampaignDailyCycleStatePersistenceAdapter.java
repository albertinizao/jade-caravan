package com.jadecaravan.adapter.out.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jadecaravan.application.campaign.port.out.CampaignDailyCycleRepository;
import com.jadecaravan.domain.campaign.CampaignDailyCycleState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CampaignDailyCycleStatePersistenceAdapter implements CampaignDailyCycleRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private final ObjectMapper objectMapper;

    public CampaignDailyCycleStatePersistenceAdapter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CampaignDailyCycleState> findByCampaignId(UUID campaignId) {
        CampaignDailyCycleStateEntity entity = entityManager.find(CampaignDailyCycleStateEntity.class, campaignId);
        if (entity == null) {
            return Optional.empty();
        }
        return Optional.of(entity.toDomain(objectMapper));
    }

    @Override
    @Transactional
    public CampaignDailyCycleState save(CampaignDailyCycleState state) {
        CampaignDailyCycleStateEntity entity = CampaignDailyCycleStateEntity.fromDomain(state, objectMapper);
        entityManager.merge(entity);
        return state;
    }
}
