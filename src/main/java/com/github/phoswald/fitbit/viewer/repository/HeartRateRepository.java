package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class HeartRateRepository {

    @PersistenceContext
    private EntityManager em;

    public List<HeartRateEntity> loadByUserIdAndDateRange(String userId, LocalDate begDate, LocalDate endDate) {
        return em.createNamedQuery("HeartRateEntity.loadByUserIdAndDateRange", HeartRateEntity.class)
                .setParameter("userId", userId)
                .setParameter("begDate", begDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public void storeAll(List<HeartRateEntity> entities) {
        for (HeartRateEntity entity : entities) {
            em.merge(entity);
        }
    }
}
