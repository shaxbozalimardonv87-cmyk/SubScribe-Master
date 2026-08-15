package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.auth.*;
import com.examplesubscribemaster.entity.*;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.exception.*;
import com.examplesubscribemaster.repository.*;
import com.examplesubscribemaster.security.JwtService;
import com.examplesubscribemaster.service.AuthService;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserRepository users;
    private final RefreshTokenRepository refresh;
    private final PasswordEncoder encoder;
    private final AuthenticationManager auth;
    private final JwtService jwt;

    public AuthServiceImpl(UserRepository u, RefreshTokenRepository r, PasswordEncoder e, AuthenticationManager a, JwtService j) {
        users = u;
        refresh = r;
        encoder = e;
        auth = a;
        jwt = j;
    }

    @Transactional
    public AuthResponse register(RegisterRequest r) {
        if (users.existsByEmail(r.email())) throw new BusinessException("Email already registered");
        User u = users.save(new User(r.email().toLowerCase(), encoder.encode(r.password()), Role.USER));
        return tokens(u);
    }

    public AuthResponse login(LoginRequest r) {
        auth.authenticate(new UsernamePasswordAuthenticationToken(r.email(), r.password()));
        return tokens(users.findByEmail(r.email().toLowerCase()).orElseThrow(() -> new ResourceNotFoundException("User not found")));
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest r) {
        RefreshToken rt = refresh.findByToken(r.refreshToken()).orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));
        if (rt.getExpiryDate().isBefore(Instant.now())) throw new UnauthorizedException("Refresh token expired");
        return tokens(rt.getUser());
    }

    private AuthResponse tokens(User u) {
        String a = jwt.generateAccessToken(u.getId(), u.getEmail(), u.getRole().name());
        String t = UUID.randomUUID().toString();
        refresh.save(new RefreshToken(t, u, Instant.now().plusSeconds(60L * 60 * 24 * 30)));
        return new AuthResponse(a, t);
    }
}
