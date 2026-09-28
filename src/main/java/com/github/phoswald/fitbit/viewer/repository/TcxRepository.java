package com.github.phoswald.fitbit.viewer.repository;

import java.util.List;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class TcxRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<TcxEntity> load(String userId, long logId) {
        return Optional.ofNullable(em.find(TcxEntity.class, new TcxEntity.TcxId(userId, logId)));
    }

    public List<TcxEntity> loadUpgradeRequiredByUserId(String userId, int maxResults) {
        return em.createNamedQuery("TcxEntity.loadUpgradeRequiredByUserId", TcxEntity.class)
                .setParameter("userId", userId)
                .setMaxResults(maxResults)
                .getResultList();
    }

    public void store(TcxEntity entity) {
        em.merge(entity);
    }
}
