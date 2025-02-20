package org.example.backend.controller;

import org.example.backend.exceptions.AuthException;
import org.example.backend.exceptions.UnauthorizedException;
import org.example.backend.exceptions.UserNotFoundException;
import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepo userRepo;

    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping(value = "active", produces = "text/plain")
    public String getActiveUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();

        if ("anonymousUser".equals(userId)) {
            return "Unauthorized";
        }

        return userId;
    }


    private static final Map<String, Object> userLocks = new ConcurrentHashMap<>();

    @PostMapping("save/{userId}")
    public synchronized String saveActiveUser(@PathVariable String userId) {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userName = null;

        if (principal instanceof OAuth2User) {
            // OAuth2 login
            userName = ((OAuth2User) principal).getAttribute("login");
        } else if (principal instanceof org.springframework.security.core.userdetails.User) {
            // Standard Spring Security login
            userName = userId; // Instead of using getUsername(), set it explicitly
        }

        if (userName == null) {
            throw new AuthException("Could not retrieve user details.");
        }

        // Ensure authenticated user matches request
        if (!SecurityContextHolder.getContext().getAuthentication().getName().equals(userId)) {
            throw new AuthException("Unauthorized: User ID mismatch!");
        }

        userLocks.putIfAbsent(userId, new Object());
        synchronized (userLocks.get(userId)) {
            try {
                String finalUserName = userName;
                return userRepo.findByGithubId(userId)
                        .map(existingUser -> {
                            return userId; // User already exists
                        })
                        .orElseGet(() -> {
                            userRepo.save(new User(null, userId, finalUserName, List.of()));
                            return userId;
                        });
            } finally {
                userLocks.remove(userId); // Clean up lock object to prevent memory leaks
            }
        }
    }

    @GetMapping("active/{userId}")
    public User getActiveUser(@PathVariable String userId) {
        return userRepo.findByGithubId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found in database."));
    }

}
