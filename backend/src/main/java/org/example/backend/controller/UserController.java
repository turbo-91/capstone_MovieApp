package org.example.backend.controller;

import org.example.backend.exceptions.AuthException;
import org.example.backend.exceptions.UserNotFoundException;
import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepo userRepo;

    public UserController(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    @GetMapping("active")
    public String getActiveUserId() throws AuthException {
            return ((OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                    .getAttribute("id").toString();
        }

    @PostMapping("save/{userId}")
    public String saveActiveUser(@PathVariable String userId) throws AuthException {
        OAuth2User oAuth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String authenticatedUserId = oAuth2User.getAttribute("id").toString();
        String userName = oAuth2User.getAttribute("login");

        if (authenticatedUserId == null) {
            throw new AuthException("Could not retrieve active user ID.");
        }
        if (userName == null) {
            throw new AuthException("Could not retrieve active user name.");
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
    }

    @GetMapping("active/{userId}")
    public User getActiveUser(@PathVariable String userId) {
        return userRepo.findByGithubId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found in database."));
    }
}
