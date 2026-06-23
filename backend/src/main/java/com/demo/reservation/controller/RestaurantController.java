package com.demo.reservation.controller;

import com.demo.reservation.domain.Restaurant;
import com.demo.reservation.repository.RestaurantRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/restaurants")
public class RestaurantController {

    private final RestaurantRepository repo;

    @Value("${app.pod-name}")
    private String podName;

    public RestaurantController(RestaurantRepository repo) {
        this.repo = repo;
    }

    // 식당 목록 (RFP의 "상품 목록 조회"에 대응)
    @GetMapping
    public Map<String, Object> list() {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("servedBy", podName);   // 어느 Pod가 응답했는지 → 로드밸런싱 증적
        res.put("restaurants", repo.findAll());
        return res;
    }

    // 식당 상세 (RFP의 "상품 상세 조회"에 대응)
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> detail(@PathVariable Long id) {
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("servedBy", podName);
        return repo.findById(id)
                .map(r -> {
                    res.put("restaurant", r);
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    res.put("error", "해당 식당을 찾을 수 없습니다.");
                    return ResponseEntity.status(404).body(res);
                });
    }
}
