package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class CardioScoreRepository {

    @PersistenceContext
    private EntityManager em;

    public List<CardioScoreEntity> loadByUserIdAndDateRange(String userId, LocalDate dateBeg, LocalDate dateEnd) {
        return em.createNamedQuery("CardioScoreEntity.loadByUserIdAndDateRange", CardioScoreEntity.class)
                .setParameter("userId", userId)
                .setParameter("dateBeg", dateBeg)
                .setParameter("dateEnd", dateEnd)
                .getResultList();
    }

    public void storeAll(Collection<CardioScoreEntity> entities) {
        for (CardioScoreEntity entity : entities) {
            em.merge(entity);
        }
    }
}
