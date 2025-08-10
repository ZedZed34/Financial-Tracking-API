package com.zz.fintrack.user;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService service;
    public UserController(UserService s){ this.service = s; }

    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody User u){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(u));
    }

    @GetMapping public List<User> list(){ return service.list(); }

    @GetMapping("{id}") public User get(@PathVariable Long id){ return service.get(id); }

    @PutMapping("{id}") public User update(@PathVariable Long id, @Valid @RequestBody User u){ return service.update(id,u); }

    @DeleteMapping("{id}") public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
