package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class CardioScoreRepository {

    @PersistenceContext
    private EntityManager em;

    public List<CardioScoreEntity> loadByUserIdAndDateRange(String userId, LocalDate begDate, LocalDate endDate) {
        return em.createNamedQuery("CardioScoreEntity.loadByUserIdAndDateRange", CardioScoreEntity.class)
                .setParameter("userId", userId)
                .setParameter("begDate", begDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public void storeAll(List<CardioScoreEntity> entities) {
        for (CardioScoreEntity entity : entities) {
            em.merge(entity);
        }
    }
}
