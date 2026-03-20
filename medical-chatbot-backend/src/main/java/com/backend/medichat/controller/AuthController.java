package com.backend.medichat.controller;

import com.backend.medichat.dto.JwtResponse;
import com.backend.medichat.dto.LoginRequest;
import com.backend.medichat.entity.User;
import com.backend.medichat.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor // Lombok sẽ tạo Constructor cho các biến 'final'
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    // THÊM TỪ KHÓA 'final' Ở ĐÂY
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}