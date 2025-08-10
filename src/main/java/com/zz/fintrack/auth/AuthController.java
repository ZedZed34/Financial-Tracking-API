package com.zz.fintrack.auth;

import com.zz.fintrack.security.JwtService;
import com.zz.fintrack.user.User;
import com.zz.fintrack.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record SignupRequest(@NotBlank @Email String email, @NotBlank String name, @NotBlank String password) {}
    public record LoginRequest(@NotBlank @Email String email, @NotBlank String password) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(UserRepository users, PasswordEncoder encoder, AuthenticationManager authManager, JwtService jwtService) {
        this.users = users; this.encoder = encoder; this.authManager = authManager; this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest req) {
        if (users.findByEmail(req.email()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "Email already registered"));
        }
        User u = User.builder()
                .email(req.email())
                .name(req.name())
                .passwordHash(encoder.encode(req.password()))
                .build();
        users.save(u);
        String token = jwtService.generateToken(u.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("token", token));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authManager.authenticate(new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        String token = jwtService.generateToken(auth.getName());
        return ResponseEntity.ok(Map.of("token", token));
    }
}


