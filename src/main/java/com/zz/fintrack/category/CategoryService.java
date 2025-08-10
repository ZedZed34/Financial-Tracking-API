package com.zz.fintrack.category;

import com.zz.fintrack.user.User;
import com.zz.fintrack.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository repo;
    private final UserService users;

    public CategoryService(CategoryRepository repo, UserService users){
        this.repo = repo; this.users = users;
    }

    public Category create(Long userId, Category in){
        User u = users.get(userId);
        in.setUser(u);
        return repo.save(in);
    }

    public List<Category> list(Long userId, CategoryType type){
        return type == null ? repo.findByUserIdAndType(userId, CategoryType.EXPENSE) // default example
                : repo.findByUserIdAndType(userId, type);
    }

    public Category get(Long id){
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    public Category update(Long id, Category in){
        var c = get(id);
        c.setName(in.getName());
        c.setType(in.getType());
        return repo.save(c);
    }

    public void delete(Long id){ repo.deleteById(id); }
}
