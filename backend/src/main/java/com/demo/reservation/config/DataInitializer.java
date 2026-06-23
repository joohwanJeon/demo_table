package com.demo.reservation.config;

import com.demo.reservation.domain.Restaurant;
import com.demo.reservation.repository.RestaurantRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

// 앱 최초 기동 시 식당 데이터가 비어 있으면 샘플 데이터를 넣어줌
@Component
public class DataInitializer implements CommandLineRunner {

    private final RestaurantRepository repo;

    public DataInitializer(RestaurantRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        repo.save(new Restaurant("정식당", "한식 파인다이닝",
                "예약 오픈과 동시에 1초 만에 마감되는 미쉐린 식당.",
                "매일 19:00 익월 예약 오픈",
                "https://picsum.photos/seed/jungsik/600/400", true));
        repo.save(new Restaurant("스시 코우지", "오마카세",
                "월 1회 예약 오픈, 오픈런 필수.",
                "매월 1일 12:00 예약 오픈",
                "https://picsum.photos/seed/sushi/600/400", true));
        repo.save(new Restaurant("라연", "한식",
                "호텔 한식당, 주말 디너 경쟁 치열.",
                "매일 10:00 예약 오픈",
                "https://picsum.photos/seed/layeon/600/400", true));
        repo.save(new Restaurant("동네 파스타", "이탈리안",
                "예약 없이도 가능한 편안한 동네 맛집.",
                "상시 예약 가능",
                "https://picsum.photos/seed/pasta/600/400", false));
    }
}
