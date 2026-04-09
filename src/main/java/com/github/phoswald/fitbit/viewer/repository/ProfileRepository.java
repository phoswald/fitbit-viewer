package com.github.phoswald.fitbit.viewer.repository;

import java.util.Optional;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ProfileRepository {

    @PersistenceContext
    private EntityManager em;

    public Optional<ProfileEntity> loadByUserId(String userId) {
        return Optional.ofNullable(em.find(ProfileEntity.class, userId));
    }

    public void store(ProfileEntity entity) {
        em.merge(entity);
    }
}
