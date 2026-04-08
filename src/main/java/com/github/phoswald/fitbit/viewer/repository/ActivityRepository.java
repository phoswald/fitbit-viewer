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
        return Optional.ofNullable(em.find(ActivityEntity.class, new ActivityEntity.ActivityId(userId, logId)));
    }

    public void storeAll(Collection<ActivityEntity> entities) {
        for (ActivityEntity entity : entities) {
            em.merge(entity);
        }
    }
}
