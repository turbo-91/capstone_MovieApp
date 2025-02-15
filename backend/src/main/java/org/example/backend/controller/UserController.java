package org.example.backend.controller;

import org.example.backend.exceptions.AuthException;
import org.example.backend.exceptions.UserNotFoundException;
import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepo userRepo;

    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("active")
    public String getActiveUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @PostMapping("save/{userId}")
    public synchronized String saveActiveUser(@PathVariable String userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("🔍 Principal Object: " + principal);

        String userName = null;

        if (principal instanceof OAuth2User) {
            // ✅ OAuth2 login
            userName = ((OAuth2User) principal).getAttribute("login");
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // ✅ Standard Spring Security login
            userName = userId; // Instead of using getUsername(), set it explicitly
        }

        if (userName == null) {
            throw new AuthException("Could not retrieve user details.");
        }

        // Ensure authenticated user matches request
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(userId)) {
            throw new AuthException("Unauthorized: User ID mismatch!");
        }

        synchronized (userId.intern()) {
            String finalUserName = userName;
            return userRepo.findByGithubId(userId)
                    .map(existingUser -> {
                        System.out.println("ℹ️ User already exists in DB.");
                        return userId; // User already exists
                    })
                    .orElseGet(() -> {
                        System.out.println("💾 Saving new user...");
                        userRepo.save(new User(null, userId, finalUserName, List.of()));
                        System.out.println("✅ User saved successfully!");
                        return userId;
                    });
        }
    }




    @GetMapping("active/{userId}")
    public User getActiveUser(@PathVariable String userId) {
        return userRepo.findByGithubId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found in database."));
    }
}
