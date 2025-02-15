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

    @GetMapping("me")
    public String getActiveUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    @GetMapping("activefeds")
    public String getPrincipal() {
        return SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
    }

    @GetMapping("username")
    public String getLogin() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Principal Object: " + principal);

        try {
            return principal.getClass().getMethod("getAttribute", String.class).invoke(principal, "login").toString();
        } catch (Exception e) {
            System.out.println("Error accessing 'login' attribute: " + e.getMessage());
            return principal.toString();
        }
    }

    @PostMapping("save/{userId}")
    public String saveActiveUser(@PathVariable String userId) throws AuthException {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        System.out.println("Principal Object: " + principal);

        try {
            // Use reflection to extract "id" and "login" attributes dynamically
            Method getAttributeMethod = principal.getClass().getMethod("getAttribute", String.class);
            String authenticatedUserId = (String) getAttributeMethod.invoke(principal, "id");
            String userName = (String) getAttributeMethod.invoke(principal, "login");

            if (authenticatedUserId == null || userName == null) {
                throw new AuthException("Could not retrieve user details.");
            }

            // Ensure that the user ID being saved matches the authenticated user's ID
            if (!authenticatedUserId.equals(userId)) {
                throw new AuthException("Unauthorized: Provided user ID does not match authenticated user.");
            }

            // Check if user already exists in the database
            Optional<User> existingUser = userRepo.findByGithubId(userId);
            if (existingUser.isPresent()) {
                System.out.println("User already exists in DB.");
                return userId; // Return ID if already exists
            }

            // Save new user
            User userToSave = new User(null, userId, userName, List.of());
            System.out.println("Saving new user in DB...");
            userRepo.save(userToSave);

            return userId;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new AuthException("Error accessing user details: " + e.getMessage());
        }
    }


    @GetMapping("active/{userId}")
    public User getActiveUser(@PathVariable String userId) {
        return userRepo.findByGithubId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found in database."));
    }
}
