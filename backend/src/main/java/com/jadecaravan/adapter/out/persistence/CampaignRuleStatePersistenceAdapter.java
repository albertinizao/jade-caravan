package com.jadecaravan.adapter.out.persistence;

import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.domain.rules.CampaignRuleState;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CampaignRuleStatePersistenceAdapter implements CampaignRulesRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public CampaignRuleState loadOrCreate(UUID campaignId) {
        CampaignRuleStateEntity entity = entityManager.find(CampaignRuleStateEntity.class, campaignId);
        if (entity == null) {
            entity = CampaignRuleStateEntity.seeded(campaignId);
            entityManager.persist(entity);
            entityManager.flush();
        }
        return entity.toDomain();
    }

    @Override
    @Transactional
    public void save(CampaignRuleState state) {
        Objects.requireNonNull(state, "state must not be null");
        CampaignRuleStateEntity entity = entityManager.find(CampaignRuleStateEntity.class, state.campaignId());
        if (entity == null) {
            entity = CampaignRuleStateEntity.seeded(state.campaignId());
            entityManager.persist(entity);
        }
        entity.replaceWith(state);
    }
}
