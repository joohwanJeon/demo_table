package com.demo.reservation.service;

import com.demo.reservation.domain.User;
import com.demo.reservation.dto.*;
import com.demo.reservation.repository.UserRepository;
import com.demo.reservation.util.JwtUtil;
import com.google.zxing.*;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import com.warrenstrange.googleauth.GoogleAuthenticatorKey;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final GoogleAuthenticator googleAuthenticator;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.googleAuthenticator = new GoogleAuthenticator();
    }

    // 회원가입
    public String register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new IllegalArgumentException("이미 존재하는 사용자입니다.");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        userRepository.save(user);
        return "회원가입이 완료되었습니다.";
    }

    // 1단계: ID/PW 로그인
    public LoginResponse login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 올바르지 않습니다.");
        }

        // OTP 미등록 시 바로 JWT 발급
        if (!user.isOtpEnabled()) {
            return new LoginResponse("SUCCESS", jwtUtil.generateToken(user.getUsername()), "로그인 성공");
        }

        // OTP 등록된 경우 2단계 인증 요청
        return new LoginResponse("OTP_REQUIRED", null, "OTP 인증이 필요합니다.");
    }

    // 2단계: OTP 검증 후 JWT 발급
    public LoginResponse verifyOtp(OtpVerifyRequest req) {
        User user = userRepository.findByUsername(req.username())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.isOtpEnabled() || user.getOtpSecret() == null) {
            throw new IllegalStateException("OTP가 설정되지 않은 사용자입니다.");
        }

        boolean valid = googleAuthenticator.authorize(user.getOtpSecret(), req.code());
        if (!valid) {
            throw new IllegalArgumentException("OTP 코드가 올바르지 않습니다.");
        }

        return new LoginResponse("SUCCESS", jwtUtil.generateToken(user.getUsername()), "로그인 성공");
    }

    // OTP 설정: QR코드 + Secret 생성
    public OtpSetupResponse setupOtp(String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        GoogleAuthenticatorKey key = googleAuthenticator.createCredentials();
        String secret = key.getKey();

        // OTP Auth URL (Google Authenticator 형식)
        String issuer = "DemoTable";
        String otpAuthUrl = "otpauth://totp/" + issuer + ":" +
            URLEncoder.encode(username, StandardCharsets.UTF_8) +
            "?secret=" + secret +
            "&issuer=" + issuer;

        // QR코드 생성 (Base64 PNG)
        String qrBase64 = generateQrCode(otpAuthUrl);

        // Secret 임시 저장 (아직 활성화 안 됨)
        user.setOtpSecret(secret);
        userRepository.save(user);

        return new OtpSetupResponse(secret, qrBase64, otpAuthUrl);
    }

    // OTP 활성화 확인 (첫 OTP 코드 검증 후 활성화)
    public String enableOtp(String username, int code) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        boolean valid = googleAuthenticator.authorize(user.getOtpSecret(), code);
        if (!valid) {
            throw new IllegalArgumentException("OTP 코드가 올바르지 않습니다. 다시 시도하세요.");
        }

        user.setOtpEnabled(true);
        userRepository.save(user);
        return "Google OTP가 활성화되었습니다.";
    }

    private String generateQrCode(String content) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            BitMatrix matrix = writer.encode(content, BarcodeFormat.QR_CODE, 300, 300);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (Exception e) {
            throw new RuntimeException("QR코드 생성 실패", e);
        }
    }
}
