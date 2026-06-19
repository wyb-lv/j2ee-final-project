package com.example.tanksolarheaterbe.controllers;

import com.example.tanksolarheaterbe.dto.LoginRequest;
import com.example.tanksolarheaterbe.dto.LoginResponse;
import com.example.tanksolarheaterbe.dto.UserRequest;
import com.example.tanksolarheaterbe.dto.UserResponse;
import com.example.tanksolarheaterbe.repositories.AccountRepository;
import com.example.tanksolarheaterbe.security.JwtService;
import com.example.tanksolarheaterbe.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AccountRepository accountRepository;
    private final AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        String token = jwtService.generateToken(request.getEmail());

        var account = accountRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return new LoginResponse(token, account.getId(), account.getName(), account.getRole());
    }

    @PostMapping("/register")
    public UserResponse register(@Valid @RequestBody UserRequest request) {
        return authService.register(request);
    }
}
