package dickoya.sendMe.controllers;

import dickoya.sendMe.DTO.AuthenticationRequest;
import dickoya.sendMe.DTO.AuthenticationResponse;
import dickoya.sendMe.DTO.RegisterRequest;
import dickoya.sendMe.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public AuthenticationResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(@Valid @RequestBody AuthenticationRequest request) {
        return authService.login(request);
    }
}