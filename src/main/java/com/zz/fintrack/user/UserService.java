package com.zz.fintrack.user;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserRepository repo;
    public UserService(UserRepository repo){ this.repo = repo; }

    public User create(User u){ return repo.save(u); }
    public List<User> list(){ return repo.findAll(); }
    public User get(Long id){ return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("User not found")); }
    public User update(Long id, User in){
        var u = get(id);
        u.setName(in.getName());
        u.setEmail(in.getEmail());
        return repo.save(u);
    }
    public void delete(Long id){ repo.deleteById(id); }
}
