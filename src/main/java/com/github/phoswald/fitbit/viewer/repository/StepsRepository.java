package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class StepsRepository {

    @PersistenceContext
    private EntityManager em;

    public List<StepsEntity> loadByUserIdAndDateRange(String userId, LocalDate begDate, LocalDate endDate) {
        return em.createNamedQuery("StepsEntity.loadByUserIdAndDateRange", StepsEntity.class)
                .setParameter("userId", userId)
                .setParameter("begDate", begDate)
                .setParameter("endDate", endDate)
                .getResultList();
    }

    public void storeAll(Collection<StepsEntity> entities) {
        for (StepsEntity entity : entities) {
            em.merge(entity);
        }
    }
}
