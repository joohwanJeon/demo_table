package com.demo.reservation.controller;

import com.demo.reservation.domain.Reservation;
import com.demo.reservation.repository.ReservationRepository;
import com.demo.reservation.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationRepository reservationRepo;
    private final RestaurantRepository restaurantRepo;

    @Value("${app.pod-name}")
    private String podName;

    public ReservationController(ReservationRepository reservationRepo,
                                 RestaurantRepository restaurantRepo) {
        this.reservationRepo = reservationRepo;
        this.restaurantRepo = restaurantRepo;
    }

    public record ReservationRequest(Long restaurantId, String customerName, Integer partySize) {}

    // 예약하기 (RFP의 "주문 요청"에 대응) — 예약 오픈 순간 트래픽이 몰리는 핵심 엔드포인트
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody ReservationRequest req) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("servedBy", podName);

        if (req.restaurantId() == null || !restaurantRepo.existsById(req.restaurantId())) {
            res.put("error", "존재하지 않는 식당입니다.");
            return ResponseEntity.status(404).body(res);
        }

        Reservation r = new Reservation();
        r.setRestaurantId(req.restaurantId());
        r.setCustomerName(req.customerName() == null ? "익명" : req.customerName());
        r.setPartySize(req.partySize() == null ? 2 : req.partySize());
        reservationRepo.save(r);

        res.put("message", "예약이 완료되었습니다.");
        res.put("reservation", r);
        res.put("totalReservations", reservationRepo.countByRestaurantId(req.restaurantId()));
        return ResponseEntity.ok(res);
    }
}
