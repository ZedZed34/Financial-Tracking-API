package com.zz.fintrack.account;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    private final AccountService service;
    public AccountController(AccountService s){ this.service = s; }

    @PostMapping
    public ResponseEntity<Account> create(@RequestParam Long userId, @Valid @RequestBody Account a){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, a));
    }

    @GetMapping
    public List<Account> list(@RequestParam Long userId){
        return service.list(userId);
    }

    @GetMapping("{id}") public Account get(@PathVariable Long id){ return service.get(id); }

    @PutMapping("{id}") public Account update(@PathVariable Long id, @Valid @RequestBody Account a){
        return service.update(id, a);
    }

    @DeleteMapping("{id}") public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
