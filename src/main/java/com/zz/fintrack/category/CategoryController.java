package com.zz.fintrack.category;

import com.zz.fintrack.common.SecurityUtils;
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
    private final SecurityUtils security;

    public CategoryController(CategoryService s, SecurityUtils security){
        this.service = s;
        this.security = security;
    }

    @PostMapping
    public ResponseEntity<Category> create(@AuthenticationPrincipal UserDetails principal,
                                           @Valid @RequestBody Category c){
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(service.create(security.currentUserId(principal), c));
    }

    @GetMapping
    public List<Category> list(@AuthenticationPrincipal UserDetails principal,
                               @RequestParam(required=false) CategoryType type){
        return service.list(security.currentUserId(principal), type);
    }

    @GetMapping("{id}")
    public Category get(@AuthenticationPrincipal UserDetails principal,
                        @PathVariable Long id){
        return service.getOwned(id, security.currentUserId(principal));
    }

    @PutMapping("{id}")
    public Category update(@AuthenticationPrincipal UserDetails principal,
                           @PathVariable Long id,
                           @Valid @RequestBody Category c){
        return service.update(id, security.currentUserId(principal), c);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable Long id){
        service.delete(id, security.currentUserId(principal));
        return ResponseEntity.noContent().build();
    }
}
