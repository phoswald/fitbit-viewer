package com.github.phoswald.fitbit.viewer.pages.labels;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class LabelSummaryRepository {

    @PersistenceContext
    private EntityManager em;

    public List<LabelSummaryEntity> loadLabelSummariesByUserId(String userId) {
        return em.createNamedQuery("LabelSummaryEntity.loadByUserId", LabelSummaryEntity.class)
                .setParameter("userId", userId)
                .getResultList();
    }
}
