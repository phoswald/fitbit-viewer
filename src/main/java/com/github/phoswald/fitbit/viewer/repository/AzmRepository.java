package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class AzmRepository {

    @PersistenceContext
    private EntityManager em;

    public List<AzmEntity> loadByUserIdAndDateRange(String userId, LocalDate begDate, LocalDate endDate) {
        return em.createNamedQuery("AzmEntity.loadByUserIdAndDateRange", AzmEntity.class)
                .setParameter("userId", userId)
                .setParameter("begDate", begDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public void storeAll(List<AzmEntity> entities) {
        for (AzmEntity entity : entities) {
            em.merge(entity);
        }
    }
}
