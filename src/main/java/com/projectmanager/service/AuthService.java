package com.projectmanager.service;

import com.projectmanager.dto.auth.AuthResponse;
import com.projectmanager.dto.auth.LoginRequest;
import com.projectmanager.dto.auth.SignupRequest;
import com.projectmanager.exception.BadRequestException;
import com.projectmanager.model.User;
import com.projectmanager.repository.UserRepository;
import com.projectmanager.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Handles user registration and login logic.
 */
@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenProvider tokenProvider;

    /**
     * Register a new user. Throws BadRequestException if email is already taken.
     */
    public AuthResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email address is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role("MEMBER")
                .build();
        user.onCreate();

        User savedUser = userRepository.save(user);

        // Generate token directly since we have the credentials
        String token = tokenProvider.generateTokenFromEmail(savedUser.getEmail());

        return AuthResponse.builder()
                .token(token)
                .id(savedUser.getId())
                .name(savedUser.getName())
                .email(savedUser.getEmail())
                .role(savedUser.getRole())
                .build();
    }

    /**
     * Authenticate an existing user and return a JWT token.
     */
    public AuthResponse login(LoginRequest request) {
        // Authenticate credentials via Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = tokenProvider.generateToken(authentication);

        // Fetch full user info for response
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("User not found"));

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}
