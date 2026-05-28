package com.zz.fintrack.account;

import com.zz.fintrack.user.User;
import com.zz.fintrack.user.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository repo;
    private final UserService users;

    public AccountService(AccountRepository repo, UserService users){
        this.repo = repo; this.users = users;
    }

    public Account create(Long userId, Account in){
        User u = users.get(userId);
        in.setUser(u);
        return repo.save(in);
    }

    public List<Account> list(Long userId){ return repo.findByUserId(userId); }

    public Account get(Long id){
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
    }

    /** Get an account and verify that it belongs to the given user. */
    public Account getOwned(Long id, Long userId){
        Account a = get(id);
        if (!a.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("You do not own this account");
        }
        return a;
    }

    public Account update(Long id, Long userId, Account in){
        var a = getOwned(id, userId);
        a.setName(in.getName());
        a.setType(in.getType());
        a.setCurrency(in.getCurrency());
        a.setInitialBalance(in.getInitialBalance());
        return repo.save(a);
    }

    public void delete(Long id, Long userId){
        getOwned(id, userId); // verify ownership
        repo.deleteById(id);
    }
}
