package com.zz.fintrack.category;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryService service;
    public CategoryController(CategoryService s){ this.service = s; }

    @PostMapping
    public ResponseEntity<Category> create(@RequestParam Long userId, @Valid @RequestBody Category c){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(userId, c));
    }

    @GetMapping
    public List<Category> list(@RequestParam Long userId,
                               @RequestParam(required=false) CategoryType type){
        return service.list(userId, type);
    }

    @GetMapping("{id}") public Category get(@PathVariable Long id){ return service.get(id); }

    @PutMapping("{id}") public Category update(@PathVariable Long id, @Valid @RequestBody Category c){
        return service.update(id, c);
    }

    @DeleteMapping("{id}") public ResponseEntity<Void> delete(@PathVariable Long id){
        service.delete(id); return ResponseEntity.noContent().build();
    }
}
