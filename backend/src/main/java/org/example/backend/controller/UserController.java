package org.example.backend.controller;

import org.example.backend.exceptions.AuthException;
import org.example.backend.exceptions.UserNotFoundException;
import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
        // ✅ Retrieve authenticated user's GitHub ID and username
        OAuth2User oAuth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        String userId = String.valueOf(oAuth2User.getAttribute("id"));  // GitHub user ID
        String userName = oAuth2User.getAttribute("login");  // GitHub username

        if (userId == null) {
            throw new AuthException("Could not retrieve active user ID.");
        }
        if (userName == null) {
            throw new AuthException("Could not retrieve active user name.");
        }

        // ✅ Check if the user already exists in the database
        Optional<User> existingUser = userRepo.findByGithubId(userId);
        if (existingUser.isPresent()) {
            return userId; // ✅ If user exists, return the ID
        }

        // ✅ If user doesn't exist, create and save them
        User userToSave = new User(null, userId, userName, List.of());
        userRepo.save(userToSave);

        return userId; // ✅ Return the ID after saving the new user
    }

    @GetMapping("active/{userId}")
    public User getActiveUser(@PathVariable String userId) {
        return userRepo.findByGithubId(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found in database."));
    }

}
