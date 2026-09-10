package com.example.EventReservationAPI.service;

import com.example.EventReservationAPI.dto.request.LoginRequest;
import com.example.EventReservationAPI.dto.request.RegisterRequest;
import com.example.EventReservationAPI.dto.response.ApiResponse;
import com.example.EventReservationAPI.dto.response.AuthResponse;
import com.example.EventReservationAPI.entity.User;
import com.example.EventReservationAPI.exception.BusinessException;
import com.example.EventReservationAPI.repository.UserRepository;
import com.example.EventReservationAPI.security.CustomUserDetails;
import com.example.EventReservationAPI.security.CustomUserDetailsService;
import com.example.EventReservationAPI.security.JwtUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService customUserDetailsService;

    @Transactional
    public ApiResponse<String> register(RegisterRequest registerRequest){
        userRepository.findByEmail(registerRequest.getEmail())
                .ifPresent(existingUser -> {
                    throw new BusinessException(
                            "Email already registered");
                });

        User newUser = User.builder()
                .name(registerRequest.getName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .role(registerRequest.getRole())
                .build();

        userRepository.save(newUser);

        return ApiResponse.success("User Registered Successfully", null);
    }

    public AuthResponse login(LoginRequest loginRequest){

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        CustomUserDetails userDetails = customUserDetailsService.loadUserByUsername(loginRequest.getEmail());

        String token = jwtUtil.generateToken(userDetails);

        return AuthResponse.builder()
                .token(token)
                .email(userDetails.getUsername())
                .role(userDetails.getUser().getRole())
                .build();
    }
}
