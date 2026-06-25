package com.demo.reservation.controller;

import com.demo.reservation.dto.*;
import com.demo.reservation.service.AuthService;
import com.demo.reservation.util.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;  
        this.jwtUtil = jwtUtil; 
    }

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody RegisterRequest req) {
        String msg = authService.register(req);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    // 1단계: ID/PW 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    // 2단계: OTP 검증
    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponse> verifyOtp(@RequestBody OtpVerifyRequest req) {
        return ResponseEntity.ok(authService.verifyOtp(req));
    }

    // OTP 설정 (QR코드 발급)
    @GetMapping("/otp/setup")
    public ResponseEntity<OtpSetupResponse> setupOtp(@RequestParam String username) {
        return ResponseEntity.ok(authService.setupOtp(username));
    }

    // OTP 활성화 (첫 코드 검증 후 확정)
    @PostMapping("/otp/enable")
    public ResponseEntity<Map<String, String>> enableOtp(
            @RequestParam String username,
            @RequestParam int code) {
        String msg = authService.enableOtp(username, code);
        return ResponseEntity.ok(Map.of("message", msg));
    }

    // 토큰 유효성 확인
    @GetMapping("/validate")
    public ResponseEntity<Map<String, Object>> validate(
            @RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.replace("Bearer ", "");
        boolean valid = jwtUtil.validateToken(token);
        String username = valid ? jwtUtil.extractUsername(token) : null;
        return ResponseEntity.ok(Map.of("valid", valid, "username", username != null ? username : ""));
    }
}
