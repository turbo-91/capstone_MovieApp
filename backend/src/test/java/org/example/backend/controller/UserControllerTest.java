package org.example.backend.controller;

import org.example.backend.exceptions.AuthException;
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
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.data.mongodb.database=testdb",
        "spring.data.mongodb.port=27017", // Ensuring MongoDB runs on a known port
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb", // Define a stable connection URI
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

        // ✅ Mock OAuth2User instead of UserDetails
        OAuth2User mockOAuth2User = mock(OAuth2User.class);
        when(mockOAuth2User.getAttribute("login")).thenReturn(TEST_USERNAME);
        when(mockOAuth2User.getAttribute("id")).thenReturn(TEST_USER_ID);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(mockOAuth2User);
        when(authentication.getName()).thenReturn(TEST_USER_ID);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
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
    void testGetActiveUserId_WithLoggedInUser_expectUserId() throws Exception {
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(content().string("user"));
    }

    @Test
    void testGetActiveUserId_WhenAnonymousUser_ShouldReturnUnauthorized() throws Exception {
        SecurityContextHolder.clearContext(); // Ensure no authentication is set

        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk()) // Expecting "Unauthorized" but with 200 OK
                .andExpect(content().string("Unauthorized"));
    }


    @Test
    void testSaveActiveUser_WhenUserNotExists_ShouldSaveUser() throws Exception {
        mockMvc.perform(post("/api/users/save/" + TEST_USER_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(TEST_USER_ID));

        // ✅ Verify the saved user has the correct username
        Optional<User> savedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(savedUser.isPresent(), "User should be saved in the database");
        assertEquals(TEST_USER_ID, savedUser.get().githubId());
        assertEquals(TEST_USERNAME, savedUser.get().username()); // Fix assertion
        assertEquals(List.of(), savedUser.get().favorites());
    }


    @Test
    void testSaveActiveUser_WhenUserDoesNotExist_ShouldSaveUser() throws Exception {
        mockMvc.perform(post("/api/users/save/" + TEST_USER_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(TEST_USER_ID));

        Optional<User> savedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(savedUser.isPresent(), "User should be saved in the database");
        assertEquals(TEST_USER_ID, savedUser.get().githubId());
        assertEquals(TEST_USERNAME, savedUser.get().username());
    }

    @Test
    void testSaveActiveUser_WhenUserExists_ShouldNotCreateDuplicate() throws Exception {
        userRepo.save(new User(null, TEST_USER_ID, TEST_USERNAME, List.of()));

        mockMvc.perform(post("/api/users/save/" + TEST_USER_ID).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string(TEST_USER_ID));

        long userCount = userRepo.count();
        assertEquals(1, userCount, "User count should still be 1, no duplicates allowed");
    }

    @Test
    @WithMockUser(username = "testUser")
    void testSaveActiveUser_WhenAuthenticated_ShouldSaveUser() throws Exception {
        mockMvc.perform(post("/api/users/save/testUser").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("testUser"));
    }

    @Test
    @WithMockUser(username = "12345")
    void testSaveActiveUser_WithMockUser_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/users/save/12345").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("12345"));
    }
    

    @Test
    void testGetActiveUser_WhenUserDoesNotExist_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(get("/api/users/active/nonexistentUser"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetActiveUser_WhenUserExists_ShouldReturnUser() throws Exception {
        // ✅ Insert the user into the database before running the test
        userRepo.save(new User(null, TEST_USER_ID, TEST_USERNAME, List.of()));

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

}
