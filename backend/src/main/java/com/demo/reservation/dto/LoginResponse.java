package com.demo.reservation.dto;

public record LoginResponse(
    String status,      // "OTP_REQUIRED" | "SUCCESS"
    String token,       // JWT (OTP 완료 후에만)
    String message
) {}
