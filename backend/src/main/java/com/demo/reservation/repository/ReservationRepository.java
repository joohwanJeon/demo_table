package com.demo.reservation.repository;

import com.demo.reservation.domain.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    long countByRestaurantId(Long restaurantId);
}
