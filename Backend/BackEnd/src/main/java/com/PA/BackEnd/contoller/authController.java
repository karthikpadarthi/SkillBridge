package com.PA.BackEnd.contoller;

import com.PA.BackEnd.dto.authResponse;
import com.PA.BackEnd.dto.loginRequest;
import com.PA.BackEnd.dto.registerRequest;
import com.PA.BackEnd.service.authService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class authController {
    private final authService authService;

    public authController(authService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public authResponse register(@RequestBody registerRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public authResponse login(@RequestBody loginRequest request) {
        return authService.login(request);
    }
}
