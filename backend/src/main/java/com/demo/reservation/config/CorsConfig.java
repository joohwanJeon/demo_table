package com.demo.reservation.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// 데모용: Nuxt 프론트에서 백엔드 API를 호출할 수 있도록 CORS 허용.
// 운영 환경에서는 allowedOrigins를 실제 프론트 도메인으로 좁히세요.
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}
