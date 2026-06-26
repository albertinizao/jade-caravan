package com.jadecaravan.adapter.out.persistence;

import com.jadecaravan.application.campaign.port.out.CampaignRulesRepository;
import com.jadecaravan.application.campaign.port.out.CampaignAuditRepository;
import com.jadecaravan.domain.campaign.CampaignAuditEntry;
import com.jadecaravan.domain.rules.CampaignRuleState;
import com.jadecaravan.domain.rules.RuleResolutionAuditEntry;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CampaignRuleStatePersistenceAdapter implements CampaignRulesRepository, CampaignAuditRepository {

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

    @Override
    public void appendAuditEntry(RuleResolutionAuditEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        CampaignRuleStateEntity stateEntity = entityManager.find(CampaignRuleStateEntity.class, entry.campaignId());
        if (stateEntity == null) {
            stateEntity = CampaignRuleStateEntity.seeded(entry.campaignId());
            entityManager.persist(stateEntity);
        }
        entityManager.persist(CampaignRuleAuditEntryEntity.fromDomain(stateEntity, entry));
    }

    @Override
    public List<RuleResolutionAuditEntry> loadAuditTrail(UUID campaignId) {
        TypedQuery<CampaignRuleAuditEntryEntity> query = entityManager.createQuery(
                "select entry from CampaignRuleAuditEntryEntity entry " +
                        "where entry.campaignRuleState.campaignId = :campaignId " +
                        "order by entry.resolvedAt asc, entry.id asc",
                CampaignRuleAuditEntryEntity.class);
        query.setParameter("campaignId", campaignId);
        return query.getResultList().stream()
                .map(entry -> entry.toDomain(campaignId))
                .toList();
    }

    @Override
    public void append(CampaignAuditEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        CampaignRuleStateEntity stateEntity = entityManager.find(CampaignRuleStateEntity.class, entry.campaignId());
        if (stateEntity == null) {
            stateEntity = CampaignRuleStateEntity.seeded(entry.campaignId());
            entityManager.persist(stateEntity);
        }
        entityManager.persist(CampaignAuditEntryEntity.fromDomain(stateEntity, entry));
    }

    @Override
    public List<CampaignAuditEntry> loadTrail(UUID campaignId) {
        TypedQuery<CampaignAuditEntryEntity> query = entityManager.createQuery(
                "select entry from CampaignAuditEntryEntity entry " +
                        "where entry.campaignRuleState.campaignId = :campaignId " +
                        "order by entry.occurredAt asc, entry.id asc",
                CampaignAuditEntryEntity.class);
        query.setParameter("campaignId", campaignId);
        return query.getResultList().stream()
                .map(entry -> entry.toDomain(campaignId))
                .toList();
    }
}
