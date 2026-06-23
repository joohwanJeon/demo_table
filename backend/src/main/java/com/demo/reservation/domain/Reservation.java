package com.demo.reservation.domain;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long restaurantId;
    private String customerName;
    private int partySize;
    private LocalDateTime reservedAt;

    public Reservation() {}

    @PrePersist
    void onCreate() {
        this.reservedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public Long getRestaurantId() { return restaurantId; }
    public String getCustomerName() { return customerName; }
    public int getPartySize() { return partySize; }
    public LocalDateTime getReservedAt() { return reservedAt; }

    public void setRestaurantId(Long restaurantId) { this.restaurantId = restaurantId; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }
    public void setPartySize(int partySize) { this.partySize = partySize; }
}
