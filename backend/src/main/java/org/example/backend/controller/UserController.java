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
        System.out.println("🔍 Principal Object: " + principal);

        try {
            // Extract user attributes
            Method getAttributeMethod = principal.getClass().getMethod("getAttribute", String.class);
            Object authenticatedUserIdObj = getAttributeMethod.invoke(principal, "id");
            String userName = (String) getAttributeMethod.invoke(principal, "login");

            // Ensure authenticatedUserId is always a String
            String authenticatedUserId = authenticatedUserIdObj.toString();

            System.out.println("✅ Extracted ID: " + authenticatedUserId);
            System.out.println("✅ Extracted Username: " + userName);

            if (authenticatedUserId == null || userName == null) {
                System.out.println("❌ Failed to retrieve user details.");
                throw new AuthException("Could not retrieve user details.");
            }

            if (!authenticatedUserId.equals(userId)) {
                System.out.println("❌ Unauthorized: User ID mismatch!");
                throw new AuthException("Unauthorized: Provided user ID does not match authenticated user.");
            }

            Optional<User> existingUser = userRepo.findByGithubId(userId);
            if (existingUser.isPresent()) {
                System.out.println("ℹ️ User already exists in DB.");
                return userId;
            }

            System.out.println("💾 Saving new user...");
            userRepo.save(new User(null, userId, userName, List.of()));

            System.out.println("✅ User saved successfully!");
            return userId;

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            System.out.println("❌ Reflection error: " + e.getMessage());
            throw new AuthException("Error accessing user details: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("❌ General error: " + e.getMessage());
            throw new AuthException("Unexpected error: " + e.getMessage());
        }
    }



    @GetMapping("active/{userId}")
    public User getActiveUser(@PathVariable String userId) {
        return userRepo.findByGithubId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found in database."));
    }
}
