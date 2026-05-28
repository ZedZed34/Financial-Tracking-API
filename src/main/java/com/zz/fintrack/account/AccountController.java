package com.zz.fintrack.account;

import com.zz.fintrack.user.User;
import com.zz.fintrack.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;
    private final UserService userService;

    public AccountController(AccountService s, UserService userService){
        this.service = s;
        this.userService = userService;
    }

    private Long currentUserId(UserDetails principal) {
        return userService.findByEmail(principal.getUsername()).getId();
    }

    @PostMapping
    public ResponseEntity<Account> create(@AuthenticationPrincipal UserDetails principal,
                                          @Valid @RequestBody Account a){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(currentUserId(principal), a));
    }

    @GetMapping
    public List<Account> list(@AuthenticationPrincipal UserDetails principal){
        return service.list(currentUserId(principal));
    }

    @GetMapping("{id}")
    public Account get(@AuthenticationPrincipal UserDetails principal,
                       @PathVariable Long id){
        return service.getOwned(id, currentUserId(principal));
    }

    @PutMapping("{id}")
    public Account update(@AuthenticationPrincipal UserDetails principal,
                          @PathVariable Long id,
                          @Valid @RequestBody Account a){
        return service.update(id, currentUserId(principal), a);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable Long id){
        service.delete(id, currentUserId(principal));
        return ResponseEntity.noContent().build();
    }
}
