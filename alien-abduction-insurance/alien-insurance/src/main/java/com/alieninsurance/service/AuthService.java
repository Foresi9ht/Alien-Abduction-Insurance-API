package com.alieninsurance.service;

import com.alieninsurance.dto.AuthDto;
import com.alieninsurance.entity.User;
import com.alieninsurance.exception.AlienInsuranceException;
import com.alieninsurance.repository.UserRepository;
import com.alieninsurance.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthDto.AuthResponse register(AuthDto.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AlienInsuranceException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AlienInsuranceException("Email already registered");
        }

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            .email(request.getEmail())
            .role("ROLE_USER")
            .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthDto.AuthResponse(token, user.getUsername(), user.getRole());
    }

    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElseThrow(() -> new AlienInsuranceException("Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new AlienInsuranceException("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthDto.AuthResponse(token, user.getUsername(), user.getRole());
    }
}
