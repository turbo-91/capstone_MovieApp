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

    @PostMapping("/watchlist/{movieSlug}")
    @ResponseStatus(HttpStatus.OK)
    public User addToWatchlist(@PathVariable String movieSlug) {
        System.out.println("addToWatchlist: Received request to add movie with slug: " + movieSlug);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("addToWatchlist: Authenticated user id: " + userId);

        if ("anonymousUser".equals(userId) || "Unauthorized".equals(userId)) {
            System.out.println("addToWatchlist: User is not authenticated. Throwing UnauthorizedException.");
            throw new UnauthorizedException("User is not authenticated.");
        }

        User user = userRepo.findByGithubId(userId)
                .orElseThrow(() -> {
                    System.out.println("addToWatchlist: User with id " + userId + " not found.");
                    return new UserNotFoundException("User with ID " + userId + " not found in database.");
                });

        System.out.println("addToWatchlist: Current watchlist for user " + user.username() + ": " + user.favorites());
        if (!user.favorites().contains(movieSlug)) {
            System.out.println("addToWatchlist: Movie not in watchlist. Adding movie with slug: " + movieSlug);
            user.favorites().add(movieSlug);
            userRepo.save(user);
            System.out.println("addToWatchlist: Movie added. New watchlist: " + user.favorites());
        } else {
            System.out.println("addToWatchlist: Movie already exists in watchlist. No action taken.");
        }

        return user;
    }

    @DeleteMapping("/watchlist/{movieSlug}")
    @ResponseStatus(HttpStatus.OK)
    public User removeFromWatchlist(@PathVariable String movieSlug) {
        System.out.println("removeFromWatchlist: Received request to remove movie with slug: " + movieSlug);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("removeFromWatchlist: Authenticated user id: " + userId);

        if ("anonymousUser".equals(userId) || "Unauthorized".equals(userId)) {
            System.out.println("removeFromWatchlist: User is not authenticated. Throwing UnauthorizedException.");
            throw new UnauthorizedException("User is not authenticated.");
        }

        User user = userRepo.findByGithubId(userId)
                .orElseThrow(() -> {
                    System.out.println("removeFromWatchlist: User with id " + userId + " not found.");
                    return new UserNotFoundException("User with ID " + userId + " not found in database.");
                });

        System.out.println("removeFromWatchlist: Current watchlist for user " + user.username() + ": " + user.favorites());
        if (user.favorites().contains(movieSlug)) {
            System.out.println("removeFromWatchlist: Movie found in watchlist. Removing movie with slug: " + movieSlug);
            user.favorites().remove(movieSlug);
            userRepo.save(user);
            System.out.println("removeFromWatchlist: Movie removed. New watchlist: " + user.favorites());
        } else {
            System.out.println("removeFromWatchlist: Movie with slug: " + movieSlug + " not found in watchlist. No action taken.");
        }

        return user;
    }

}
