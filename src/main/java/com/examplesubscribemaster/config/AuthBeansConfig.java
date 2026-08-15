package com.examplesubscribemaster.config;

import org.springframework.context.annotation.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.examplesubscribemaster.repository.UserRepository;

@Configuration
public class AuthBeansConfig {
    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration c) throws Exception {
        return c.getAuthenticationManager();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository repo) {
        return email -> repo.findByEmail(email.toLowerCase()).map(u -> new User(u.getEmail(), u.getPassword(), java.util.List.of(new SimpleGrantedAuthority("ROLE_" + u.getRole().name())))).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}
