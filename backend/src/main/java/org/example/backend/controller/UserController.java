package org.example.backend.controller;

import org.example.backend.model.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("/me")
    public User getMe(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            return null;
        }

        String githubId = oAuth2User.getAttribute("id"); // Extract GitHub ID
        String username = oAuth2User.getAttribute("login"); // Extract GitHub username

        return new User(null, username, githubId, List.of()); // Assuming empty favorites for now
    }
}
