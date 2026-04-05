package com.github.phoswald.fitbit.viewer.repository;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class TcxRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<TcxEntity> load(String userId, Long logId) {
        return Optional.ofNullable(em.find(TcxEntity.class, new TcxEntity.TcxId(userId, logId)));
    }

    public void store(TcxEntity entity) {
        em.merge(entity);
    }
}
