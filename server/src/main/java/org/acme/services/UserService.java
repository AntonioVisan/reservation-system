package org.acme.services;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.acme.entity.User;

import java.util.UUID;

@ApplicationScoped
public class UserService {

    @Inject
    EntityManager em;

    @Transactional
    public User createTemporaryUser() {
        User user = new User("temp-" + UUID.randomUUID(), "N/A");
        em.persist(user);
        return user;
    }
}
