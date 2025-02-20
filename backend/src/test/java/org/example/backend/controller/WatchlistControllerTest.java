package org.example.backend.controller;

import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.data.mongodb.database=testdb",
        "spring.data.mongodb.port=27017",
        "spring.data.mongodb.uri=mongodb://localhost:27017/testdb",
        "TMDB_API_KEY=dummy-api-key",
        "NETZKINO_ENV=test-environment"
})
class WatchlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepo userRepo;

    private final String TEST_USER_ID = "12345";
    private final String TEST_USERNAME = "testUser";
    private final String TEST_MOVIE_SLUG = "test-movie";

    @BeforeEach
    void setUp() {
        userRepo.deleteAll();
        // Create and save a user in the test database
        userRepo.save(new User(null, TEST_USER_ID, TEST_USERNAME, List.of()));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testCheckMovieNotInWatchlist_ShouldReturnFalse() throws Exception {
        mockMvc.perform(get("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"inWatchlist\":false}"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testAddMovieToWatchlist_ShouldSucceed() throws Exception {
        mockMvc.perform(post("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Movie added to watchlist."));

        // Verify in database
        Optional<User> updatedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(updatedUser.isPresent(), "User should exist in the database");
        assertTrue(updatedUser.get().favorites().contains(TEST_MOVIE_SLUG), "Movie should be in watchlist");
    }

    @Test
    @WithMockUser(username = "testUser")
    void testRemoveMovieFromWatchlist_ShouldSucceed() throws Exception {
        // First, add the movie to the watchlist
        mockMvc.perform(post("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk());

        // Then, remove it from the watchlist
        mockMvc.perform(delete("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Movie removed from watchlist."));

        // Verify in database
        Optional<User> updatedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(updatedUser.isPresent(), "User should exist in the database");
        assertFalse(updatedUser.get().favorites().contains(TEST_MOVIE_SLUG), "Movie should be removed from watchlist");
    }

    @Test
    @WithMockUser(username = "testUser")
    void testAddThenCheckMovieInWatchlist_ShouldReturnTrue() throws Exception {
        // Add movie
        mockMvc.perform(post("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk());

        // Check if it's in the watchlist
        mockMvc.perform(get("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"inWatchlist\":true}"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testRemoveMovieFromEmptyWatchlist_ShouldReturnSuccess() throws Exception {
        // Try removing a movie that was never added
        mockMvc.perform(delete("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Movie removed from watchlist.")); // Should still return success

        // Verify in database
        Optional<User> updatedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(updatedUser.isPresent(), "User should exist in the database");
        assertFalse(updatedUser.get().favorites().contains(TEST_MOVIE_SLUG), "Watchlist should still be empty");
    }

    @Test
    @WithMockUser(username = "testUser")
    void testCheckWatchlistForNonExistentUser_ShouldReturnFalse() throws Exception {
        mockMvc.perform(get("/api/users/watchlist/nonexistent-user/" + TEST_MOVIE_SLUG))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"inWatchlist\":false}"));
    }

    @Test
    @WithMockUser(username = "testUser")
    void testAddDuplicateMovieToWatchlist_ShouldNotCreateDuplicateEntry() throws Exception {
        // Add the movie twice
        mockMvc.perform(post("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/users/watchlist/" + TEST_USER_ID + "/" + TEST_MOVIE_SLUG).with(csrf()))
                .andExpect(status().isOk());

        // Verify in database (should only contain the movie once)
        Optional<User> updatedUser = userRepo.findByGithubId(TEST_USER_ID);
        assertTrue(updatedUser.isPresent(), "User should exist in the database");
        assertEquals(1, updatedUser.get().favorites().stream()
                .filter(slug -> slug.equals(TEST_MOVIE_SLUG)).count(), "Movie should not be duplicated");
    }
}
