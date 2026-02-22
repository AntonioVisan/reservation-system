package org.acme.entity;

import jakarta.persistence.*;

@Entity
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Long slotId;

    public Reservation(User user, Long slotId) {
        this.user = user;
        this.slotId = slotId;
    }

    public Reservation() { }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Long getSlotId() {
        return slotId;
    }

    public void setSlotId(Long slotId) {
        this.slotId = slotId;
    }

    public Long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Reservation{" +
                "id=" + id +
                ", username=" + (user != null ? user.getUsername() : null) +
                ", slotId=" + slotId + '}';
    }
}
