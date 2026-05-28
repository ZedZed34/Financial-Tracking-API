package com.zz.fintrack.user;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService s){ this.service = s; }

    /** Get the currently authenticated user's profile. */
    @GetMapping("/me")
    public User me(@AuthenticationPrincipal UserDetails principal) {
        return service.findByEmail(principal.getUsername());
    }

    /** Update the currently authenticated user's profile. */
    @PutMapping("/me")
    public User updateMe(@AuthenticationPrincipal UserDetails principal,
                         @Valid @RequestBody User u) {
        User current = service.findByEmail(principal.getUsername());
        return service.update(current.getId(), u);
    }

    /** Delete the currently authenticated user's account. */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMe(@AuthenticationPrincipal UserDetails principal) {
        User current = service.findByEmail(principal.getUsername());
        service.delete(current.getId());
        return ResponseEntity.noContent().build();
    }
}
