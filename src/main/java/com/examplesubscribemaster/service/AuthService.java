package com.examplesubscribemaster.service;

import com.examplesubscribemaster.dto.auth.*;

public interface AuthService {
    AuthResponse register(RegisterRequest r);

    AuthResponse login(LoginRequest r);

    AuthResponse refresh(RefreshRequest r);
}
