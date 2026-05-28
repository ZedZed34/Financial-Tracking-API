package com.zz.fintrack.category;

import com.zz.fintrack.user.UserService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    private final UserService userService;

    public CategoryController(CategoryService s, UserService userService){
        this.service = s;
        this.userService = userService;
    }

    private Long currentUserId(UserDetails principal) {
        return userService.findByEmail(principal.getUsername()).getId();
    }

    @PostMapping
    public ResponseEntity<Category> create(@AuthenticationPrincipal UserDetails principal,
                                           @Valid @RequestBody Category c){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(currentUserId(principal), c));
    }

    @GetMapping
    public List<Category> list(@AuthenticationPrincipal UserDetails principal,
                               @RequestParam(required=false) CategoryType type){
        return service.list(currentUserId(principal), type);
    }

    @GetMapping("{id}")
    public Category get(@AuthenticationPrincipal UserDetails principal,
                        @PathVariable Long id){
        return service.getOwned(id, currentUserId(principal));
    }

    @PutMapping("{id}")
    public Category update(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long id,
                           @Valid @RequestBody Category c){
        return service.update(id, currentUserId(principal), c);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable Long id){
        service.delete(id, currentUserId(principal));
        return ResponseEntity.noContent().build();
    }
}
