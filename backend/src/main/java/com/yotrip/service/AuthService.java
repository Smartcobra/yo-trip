package com.yotrip.service;

import com.yotrip.config.AppProperties;
import com.yotrip.entity.OtpSession;
import com.yotrip.entity.User;
import com.yotrip.exception.BadRequestException;
import com.yotrip.repository.OtpSessionRepository;
import com.yotrip.repository.UserRepository;
import com.yotrip.dto.*;
import com.yotrip.security.JwtService;
import com.yotrip.util.CustomerIdFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final OtpSessionRepository otpSessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AppProperties appProperties;
    private final SecureRandom random = new SecureRandom();

    public AuthService(UserRepository userRepository,
                       OtpSessionRepository otpSessionRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       AppProperties appProperties) {
        this.userRepository = userRepository;
        this.otpSessionRepository = otpSessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.appProperties = appProperties;
    }

    @Transactional
    public void sendOtp(SendOtpRequest request) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        OtpSession session = new OtpSession();
        session.setMobile(request.mobile());
        session.setOtpHash(passwordEncoder.encode(otp));
        session.setExpiresAt(Instant.now().plus(appProperties.getOtp().getExpiryMinutes(), ChronoUnit.MINUTES));
        otpSessionRepository.deleteByMobile(request.mobile());
        otpSessionRepository.save(session);
        log.info("OTP for {}: {} (dev mode)", request.mobile(), otp);
    }

    @Transactional
    public AuthResponse verifyOtp(VerifyOtpRequest request) {
        OtpSession session = otpSessionRepository.findTopByMobileOrderByIdDesc(request.mobile())
                .orElseThrow(() -> new BadRequestException("OTP not found. Please request a new one."));

        if (session.getExpiresAt().isBefore(Instant.now())) {
            throw new BadRequestException("OTP has expired");
        }
        if (!passwordEncoder.matches(request.otp(), session.getOtpHash())) {
            throw new BadRequestException("Invalid OTP");
        }

        session.setVerified(true);
        otpSessionRepository.save(session);

        boolean isNewUser = !userRepository.existsByMobile(request.mobile());
        User user;

        if (isNewUser) {
            if (request.name() == null || request.email() == null || request.password() == null) {
                throw new BadRequestException("Name, email and password are required for new users");
            }
            if (userRepository.existsByEmail(request.email())) {
                throw new BadRequestException("Email already registered");
            }
            user = new User();
            user.setName(request.name());
            user.setEmail(request.email());
            user.setMobile(request.mobile());
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            user = userRepository.save(user);
            user.setCustomerId(CustomerIdFormatter.format(user.getId()));
            user = userRepository.save(user);
        } else {
            user = userRepository.findByMobile(request.mobile())
                    .orElseThrow(() -> new NotFoundUserException());
            user = ensureCustomerId(user);
        }

        String token = jwtService.generateToken(user.getId(), user.getMobile());
        return new AuthResponse(token, toUserResponse(user), isNewUser);
    }

    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByNameIgnoreCase(request.username())
                .or(() -> userRepository.findByEmail(request.username()))
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        user = ensureCustomerId(user);

        String token = jwtService.generateToken(user.getId(), user.getMobile());
        return new AuthResponse(token, toUserResponse(user), false);
    }

    public UserResponse getCurrentUser(User user) {
        return toUserResponse(ensureCustomerId(user));
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getCustomerId(),
                user.getName(),
                user.getEmail(),
                user.getMobile()
        );
    }

    private User ensureCustomerId(User user) {
        if (user.getCustomerId() != null && !user.getCustomerId().isBlank()) {
            return user;
        }
        user.setCustomerId(CustomerIdFormatter.format(user.getId()));
        return userRepository.save(user);
    }

    private static class NotFoundUserException extends BadRequestException {
        NotFoundUserException() {
            super("User not found");
        }
    }
}
