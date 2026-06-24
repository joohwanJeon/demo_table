package com.demo.reservation.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // Google OTP 비밀키 (Base32 인코딩)
    @Column(name = "otp_secret")
    private String otpSecret;

    // OTP 등록 완료 여부
    @Column(name = "otp_enabled")
    private boolean otpEnabled = false;

    public User() {}

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getOtpSecret() { return otpSecret; }
    public boolean isOtpEnabled() { return otpEnabled; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setOtpSecret(String otpSecret) { this.otpSecret = otpSecret; }
    public void setOtpEnabled(boolean otpEnabled) { this.otpEnabled = otpEnabled; }
}
