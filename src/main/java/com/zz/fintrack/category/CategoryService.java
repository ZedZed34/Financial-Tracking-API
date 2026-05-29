package com.zz.fintrack.category;

import com.zz.fintrack.user.User;
import com.zz.fintrack.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    private final UserService users;

    public CategoryService(CategoryRepository repo, UserService users){
        this.repo = repo; this.users = users;
    }

    @CacheEvict(value = "categories", key = "#userId + '-ALL'")
    public Category create(Long userId, Category in){
        User u = users.get(userId);
        in.setUser(u);
        return repo.save(in);
    }

    @Cacheable(value = "categories", key = "#userId + '-' + (#type != null ? #type : 'ALL')")
    public List<Category> list(Long userId, CategoryType type){
        // Ensure returning all categories if type is not specified (Wait, the original code had a bug! It defaulted to EXPENSE if type was null instead of finding all!)
        return type == null ? repo.findByUserId(userId) 
                : repo.findByUserIdAndType(userId, type);
    }

    public Category get(Long id){
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public Category getOwned(Long id, Long userId){
        Category c = get(id);
        if (!c.getUser().getId().equals(userId)) {
            throw new org.springframework.security.access.AccessDeniedException("You do not own this category");
        }
        return c;
    }

    @CacheEvict(value = "categories", allEntries = true)
    public Category update(Long id, Long userId, Category in){
        var c = getOwned(id, userId);
        c.setName(in.getName());
        c.setType(in.getType());
        return repo.save(c);
    }

    @CacheEvict(value = "categories", allEntries = true)
    public void delete(Long id, Long userId){ 
        getOwned(id, userId);
        repo.deleteById(id); 
    }
}
