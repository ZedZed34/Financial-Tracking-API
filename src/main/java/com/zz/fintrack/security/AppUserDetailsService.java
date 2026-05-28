package com.zz.fintrack.security;

import com.zz.fintrack.user.User;
import com.zz.fintrack.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppUserDetailsService implements UserDetailsService {
    private final UserRepository users;
    public AppUserDetailsService(UserRepository users) { this.users = users; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User u = users.findByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        String hash = u.getPasswordHash();
        if (hash == null || hash.isBlank()) {
            throw new UsernameNotFoundException("Account not fully set up");
        }
        return new org.springframework.security.core.userdetails.User(u.getEmail(), hash,
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }
}


