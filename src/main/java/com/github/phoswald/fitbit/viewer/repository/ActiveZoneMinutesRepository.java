package com.github.phoswald.fitbit.viewer.repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@ApplicationScoped
public class ActiveZoneMinutesRepository {

    @PersistenceContext
    private EntityManager em;

    public List<ActiveZoneMinutesEntity> loadByUserIdAndDateRange(String userId, LocalDate dateBeg, LocalDate dateEnd) {
        return em.createNamedQuery("ActiveZoneMinutesEntity.loadByUserIdAndDateRange", ActiveZoneMinutesEntity.class)
                .setParameter("userId", userId)
                .setParameter("dateBeg", dateBeg)
                .setParameter("dateEnd", dateEnd)
                .getResultList();
    }

    public void storeAll(Collection<ActiveZoneMinutesEntity> entities) {
        for (ActiveZoneMinutesEntity entity : entities) {
            em.merge(entity);
        }
    }
}
