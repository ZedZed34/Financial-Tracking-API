package com.zz.fintrack.common;

import com.zz.fintrack.user.UserService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Shared helper to resolve the authenticated user's database ID from a UserDetails principal.
 */
@Component
public class SecurityUtils {

    private final UserService userService;

    public SecurityUtils(UserService userService) {
        this.userService = userService;
    }

    public Long currentUserId(UserDetails principal) {
        return userService.findByEmail(principal.getUsername()).getId();
    }
}
