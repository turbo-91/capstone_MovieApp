package org.example.backend.service;

import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WatchlistServiceTest {

    private UserRepo userRepo;
    private WatchlistService watchlistService;

    private final String TEST_GITHUB_ID = "12345";
    private final String TEST_MOVIE_SLUG = "test-movie";

    @BeforeEach
    void setUp() {
        userRepo = mock(UserRepo.class);
        watchlistService = new WatchlistService(userRepo);
    }

    @Test
    void isMovieInWatchlist_ShouldReturnTrue_WhenMovieIsInWatchlist() {
        // GIVEN
        User user = new User("1", TEST_GITHUB_ID, "testUser", List.of(TEST_MOVIE_SLUG));
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.of(user));

        // WHEN
        boolean result = watchlistService.isMovieInWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        assertTrue(result);
        verify(userRepo).findByGithubId(TEST_GITHUB_ID);
    }

    @Test
    void isMovieInWatchlist_ShouldReturnFalse_WhenMovieIsNotInWatchlist() {
        // GIVEN
        User user = new User("1", TEST_GITHUB_ID, "testUser", List.of("other-movie"));
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.of(user));

        // WHEN
        boolean result = watchlistService.isMovieInWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        assertFalse(result);
        verify(userRepo).findByGithubId(TEST_GITHUB_ID);
    }

    @Test
    void isMovieInWatchlist_ShouldReturnFalse_WhenUserNotFound() {
        // GIVEN
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.empty());

        // WHEN
        boolean result = watchlistService.isMovieInWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        assertFalse(result);
        verify(userRepo).findByGithubId(TEST_GITHUB_ID);
    }

    @Test
    void addToWatchlist_ShouldAddMovie_WhenNotAlreadyInWatchlist() {
        // GIVEN
        User user = new User("1", TEST_GITHUB_ID, "testUser", new ArrayList<>());
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.of(user));

        // WHEN
        watchlistService.addToWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        verify(userRepo).save(any(User.class));
    }

    @Test
    void addToWatchlist_ShouldNotAddDuplicateMovie() {
        // GIVEN
        User user = new User("1", TEST_GITHUB_ID, "testUser", new ArrayList<>(List.of(TEST_MOVIE_SLUG)));
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.of(user));

        // WHEN
        watchlistService.addToWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        verify(userRepo, never()).save(any(User.class)); // No save should happen
    }

    @Test
    void addToWatchlist_ShouldDoNothing_WhenUserNotFound() {
        // GIVEN
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.empty());

        // WHEN
        watchlistService.addToWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        verify(userRepo, never()).save(any(User.class)); // No save should happen
    }

    @Test
    void removeFromWatchlist_ShouldRemoveMovie_WhenExists() {
        // GIVEN
        User user = new User("1", TEST_GITHUB_ID, "testUser", new ArrayList<>(List.of(TEST_MOVIE_SLUG)));
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.of(user));

        // WHEN
        watchlistService.removeFromWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        verify(userRepo).save(any(User.class));
    }

    @Test
    void removeFromWatchlist_ShouldDoNothing_WhenMovieNotInWatchlist() {
        // GIVEN
        User user = new User("1", TEST_GITHUB_ID, "testUser", new ArrayList<>(List.of("other-movie")));
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.of(user));

        // WHEN
        watchlistService.removeFromWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        verify(userRepo, never()).save(any(User.class)); // No save should happen
    }

    @Test
    void removeFromWatchlist_ShouldDoNothing_WhenUserNotFound() {
        // GIVEN
        when(userRepo.findByGithubId(TEST_GITHUB_ID)).thenReturn(Optional.empty());

        // WHEN
        watchlistService.removeFromWatchlist(TEST_GITHUB_ID, TEST_MOVIE_SLUG);

        // THEN
        verify(userRepo, never()).save(any(User.class)); // No save should happen
    }
}
