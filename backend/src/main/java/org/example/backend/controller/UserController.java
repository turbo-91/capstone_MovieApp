package org.example.backend.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @GetMapping("active")
    public String getActiveUserId() {
        String userId = ((OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal())
                .getAttribute("id").toString();
        System.out.println("github User id" + userId);
        return userId;
    }

}
