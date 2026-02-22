package org.acme.services;

import jakarta.persistence.EntityManager;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.LockModeType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.acme.entity.Reservation;
import org.acme.entity.Slot;
import org.acme.entity.User;

import java.util.List;

@ApplicationScoped
public class ReservationService {

    public enum ReservationResult {
        SUCCESS,
        SLOT_NOT_FOUND,
        SLOT_ALREADY_RESERVED
    }
    @Inject
    EntityManager em;

    @Transactional
    public ReservationResult createReservation(User user, Long slotId) {
        Slot slot = em.find(Slot.class, slotId, LockModeType.PESSIMISTIC_WRITE);
        if (slot == null) {
            return ReservationResult.SLOT_NOT_FOUND;
        }

        boolean alreadyReserved = em.createQuery("select count(r) from Reservation r where r.slotId = :slotId", Long.class)
                .setParameter("slotId", slotId)
                .getSingleResult() > 0;

        if (alreadyReserved) {
            return ReservationResult.SLOT_ALREADY_RESERVED;
        }

        Reservation reservation = new Reservation(user, slotId);
        em.persist(reservation);
        return ReservationResult.SUCCESS;
    }

    @Transactional
    public List<Reservation> getAllReservationsByUser(User user) {
        return em.createQuery("select r from Reservation r where r.user = :user", Reservation.class)
                .setParameter("user", user)
                .getResultList();
    }

    @Transactional
    public boolean cancelReservation(Long reservationId, User user) {
        Reservation reservation = em.find(Reservation.class, reservationId);
        if(reservation == null) {
            return false;
        }
        if (!reservation.getUser().getId().equals(user.getId())) {
            return false;
        }
        em.remove(reservation);
        return true;
    }
}
