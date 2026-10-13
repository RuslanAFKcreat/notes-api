package com.example.NotesApi.controller;

import com.example.NotesApi.dto.AuthRequest;
import com.example.NotesApi.dto.AuthResponse;
import com.example.NotesApi.entity.User;
import com.example.NotesApi.security.JwtService;
import com.example.NotesApi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody AuthRequest request) {
        // 1. Создаём пользователя (пароль хешируется в UserService)
        User user = userService.createUserEntity(request);

        // 2. Сразу логиним — генерируем токен
        String token = jwtService.generateToken(user);

        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // 1. Аутентификация через AuthenticationManager
        //    Он использует CustomUserDetailsService + BCrypt
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. Если прошло — генерируем токен
        UserDetails userDetails = userService.loadUserByEmail(request.getEmail());
        String token = jwtService.generateToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder().token(token).build());
    }
}