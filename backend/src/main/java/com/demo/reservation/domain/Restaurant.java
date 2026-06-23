package com.demo.reservation.domain;

import jakarta.persistence.*;

@Entity
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String cuisine;
    @Column(length = 1000)
    private String description;
    private String openTime;   // 예: "매일 19:00 예약 오픈"
    private String imageUrl;
    private boolean popular;    // 예약 오픈런이 일어나는 인기 식당 여부

    public Restaurant() {}

    public Restaurant(String name, String cuisine, String description,
                      String openTime, String imageUrl, boolean popular) {
        this.name = name;
        this.cuisine = cuisine;
        this.description = description;
        this.openTime = openTime;
        this.imageUrl = imageUrl;
        this.popular = popular;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getCuisine() { return cuisine; }
    public String getDescription() { return description; }
    public String getOpenTime() { return openTime; }
    public String getImageUrl() { return imageUrl; }
    public boolean isPopular() { return popular; }
}
