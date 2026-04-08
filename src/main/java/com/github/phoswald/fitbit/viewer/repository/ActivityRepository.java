package com.github.phoswald.fitbit.viewer.repository;

import java.util.Collection;
import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ActivityRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<ActivityEntity> loadByUserIdAndLogId(String userId, long logId) {
        return Optional.ofNullable(em.createNamedQuery("ActivityEntity.loadByUserIdAndLogId", ActivityEntity.class)
                .setParameter("userId", userId)
                .setParameter("logId", logId)
                .getSingleResultOrNull());
    }

    public void storeAll(Collection<ActivityEntity> entities) {
        for (ActivityEntity entity : entities) {
            em.merge(entity);
        }
    }
}
