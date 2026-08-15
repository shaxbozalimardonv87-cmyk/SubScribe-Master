package com.examplesubscribemaster.service.impl;

import com.examplesubscribemaster.dto.auth.*;
import com.examplesubscribemaster.entity.RefreshToken;
import com.examplesubscribemaster.entity.User;
import com.examplesubscribemaster.enums.Role;
import com.examplesubscribemaster.exception.BusinessException;
import com.examplesubscribemaster.exception.ResourceNotFoundException;
import com.examplesubscribemaster.exception.UnauthorizedException;
import com.examplesubscribemaster.repository.RefreshTokenRepository;
import com.examplesubscribemaster.repository.UserRepository;
import com.examplesubscribemaster.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository users;
    @Mock private RefreshTokenRepository refresh;
    @Mock private PasswordEncoder encoder;
    @Mock private AuthenticationManager auth;
    @Mock private JwtService jwt;

    private AuthServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new AuthServiceImpl(users, refresh, encoder, auth, jwt);
    }

    @Test
    void register_savesEncodedPasswordAndReturnsTokens() {
        RegisterRequest request = new RegisterRequest("New@Example.com", "password123");
        when(users.existsByEmail("New@Example.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("encoded-hash");
        when(users.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(jwt.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(refresh.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = service.register(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        assertThat(response.refreshToken()).isNotBlank();
        verify(encoder).encode("password123");
        verify(users).save(argThatUserHasEmailAndRole());
    }

    private User argThatUserHasEmailAndRole() {
        return org.mockito.ArgumentMatchers.argThat(u ->
            u.getEmail().equals("new@example.com")
                && u.getPassword().equals("encoded-hash")
                && u.getRole() == Role.USER);
    }

    @Test
    void register_duplicateEmail_throwsBusinessException() {
        RegisterRequest request = new RegisterRequest("dup@example.com", "password123");
        when(users.existsByEmail("dup@example.com")).thenReturn(true);

        assertThatThrownBy(() -> service.register(request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("already registered");

        verify(users, never()).save(any());
    }

    @Test
    void login_wrongPassword_propagatesAuthenticationException() {
        LoginRequest request = new LoginRequest("user@example.com", "wrong-password");
        when(auth.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        assertThatThrownBy(() -> service.login(request)).isInstanceOf(BadCredentialsException.class);
        verifyNoInteractions(jwt);
    }

    @Test
    void login_validCredentials_returnsTokens() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = new User("user@example.com", "encoded-hash", Role.USER);
        when(users.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwt.generateAccessToken(any(), anyString(), anyString())).thenReturn("access-token");
        when(refresh.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = service.login(request);

        assertThat(response.accessToken()).isEqualTo("access-token");
        verify(auth).authenticate(any());
    }

    @Test
    void refresh_validToken_issuesNewTokenPair() {
        User user = new User("user@example.com", "hash", Role.USER);
        RefreshToken token = new RefreshToken("old-token", user, Instant.now().plusSeconds(3600));
        when(refresh.findByToken("old-token")).thenReturn(Optional.of(token));
        when(jwt.generateAccessToken(any(), anyString(), anyString())).thenReturn("new-access-token");
        when(refresh.save(any(RefreshToken.class))).thenAnswer(inv -> inv.getArgument(0));

        AuthResponse response = service.refresh(new RefreshRequest("old-token"));

        assertThat(response.accessToken()).isEqualTo("new-access-token");
    }

    @Test
    void refresh_expiredToken_throwsUnauthorized() {
        User user = new User("user@example.com", "hash", Role.USER);
        RefreshToken token = new RefreshToken("expired-token", user, Instant.now().minusSeconds(3600));
        when(refresh.findByToken("expired-token")).thenReturn(Optional.of(token));

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("expired-token")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("expired");
    }

    @Test
    void refresh_unknownToken_throwsUnauthorized() {
        when(refresh.findByToken("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.refresh(new RefreshRequest("missing")))
            .isInstanceOf(UnauthorizedException.class)
            .hasMessageContaining("Invalid refresh token");
    }

    @Test
    void register_userNotPersisted_stillDoesNotSwallowLookupFailures() {
        when(users.existsByEmail(anyString())).thenReturn(false);
        when(encoder.encode(anyString())).thenReturn("hash");
        when(users.save(any(User.class))).thenThrow(new ResourceNotFoundException("db unavailable"));

        assertThatThrownBy(() -> service.register(new RegisterRequest("x@example.com", "password123")))
            .isInstanceOf(ResourceNotFoundException.class);
    }
}
