package com.example.EventReservationAPI.controller;

import com.example.EventReservationAPI.dto.request.LoginRequest;
import com.example.EventReservationAPI.dto.request.RegisterRequest;
import com.example.EventReservationAPI.dto.response.ApiResponse;
import com.example.EventReservationAPI.dto.response.AuthResponse;
import com.example.EventReservationAPI.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> registerMethod(@Valid @RequestBody RegisterRequest registerRequest){
        ApiResponse apiResponse = authService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> loginMethod(@Valid @RequestBody LoginRequest loginRequest){
        AuthResponse authResponse = authService.login(loginRequest);

        return ResponseEntity.ok(ApiResponse.success("Login Successfull" , authResponse));
    }
}
