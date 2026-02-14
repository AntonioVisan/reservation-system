package org.acme.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.acme.entity.Slot;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import java.util.List;

@ApplicationScoped
public class SlotService {
    @Inject
    EntityManager em;

    @Transactional
    public List<Slot> getAvailableSlots() {
        return em.createQuery("""
            select s from Slot s
            where s.id not in (
                select r.slotId from Reservation r
            )""", Slot.class).getResultList();
    }
}
