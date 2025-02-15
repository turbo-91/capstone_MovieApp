package org.example.backend.controller;

import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "TMDB_API_KEY=dummy-api-key",
        "NETZKINO_ENV=test-environment"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    private final String TEST_USER_ID = "12345";
    private final String TEST_USERNAME = "testUser";

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();

        // Ensure the user is persisted before testing
        User testUser = new User(null, TEST_USER_ID, TEST_USERNAME, List.of());
        userRepo.save(testUser);

        assertTrue(userRepo.findByGithubId(TEST_USER_ID).isPresent(), "User should exist in database before test runs");
    }

    void mockAuthenticatedUser() {
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("id")).thenReturn(TEST_USER_ID);
        when(mockOAuth2User.getAttribute("login")).thenReturn(TEST_USERNAME);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockOAuth2User);
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    @WithMockUser
    void testGetMe_WithLoggedInUser_expectUserId() throws Exception {
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(content().string("user"));
    }

    @Test
    @WithMockUser(username = "12345") // Ensures an authenticated user for basic security
    void testSaveActiveUser_WhenUserNotExists_ShouldSaveUser() throws Exception {
        mockAuthenticatedUser(); // Ensures OAuth2User authentication is set

        mockMvc.perform(post("/api/users/save/" + TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().string(TEST_USER_ID));

        // Verify user is saved
        Optional<User> savedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(savedUser.isPresent(), "User should be saved in the database");
        assertEquals(TEST_USER_ID, savedUser.get().githubId());
        assertEquals(TEST_USERNAME, savedUser.get().username());
        assertEquals(List.of(), savedUser.get().favorites());
    }

    @Test
    @WithMockUser(username = "12345") // Ensures a logged-in user is detected
    void testGetActiveUser_WhenUserExists_ShouldReturnUser() throws Exception {
        mockAuthenticatedUser(); // Ensure OAuth2User authentication

        // Debug: Check if the user exists in the database
        System.out.println("DEBUG: Users in DB -> " + userRepo.findAll());

        mockMvc.perform(get("/api/users/active/" + TEST_USER_ID))
                .andExpect(status().isOk())
                .andExpect(content().json("""
                    {
                        "githubId": "12345",
                        "username": "testUser",
                        "favorites": []
                    }
                """));
    }

    @Test
    void testGetActiveUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/active/nonexistentUser"))
                .andExpect(status().isNotFound());
    }}