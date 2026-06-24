package com.demo.reservation.dto;

public record OtpSetupResponse(
    String secret,
    String qrCodeBase64,
    String otpAuthUrl
) {}
