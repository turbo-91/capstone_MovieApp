package org.example.backend.service;

import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class WatchlistService {

    private final UserRepo userRepo;

    public WatchlistService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    // ✅ Check if a movie is in the user's watchlist
    public boolean isMovieInWatchlist(String githubId, String movieSlug) {
        System.out.println("Checking if movie " + movieSlug + " is in watchlist for user " + githubId);
        Optional<User> userOpt = userRepo.findByGithubId(githubId);
        return userOpt.map(user -> user.favorites().contains(movieSlug)).orElse(false);
    }

    // ✅ Add a movie to the watchlist
    public void addToWatchlist(String githubId, String movieSlug) {
        System.out.println("Adding movie " + movieSlug + " to watchlist for user " + githubId);
        Optional<User> userOpt = userRepo.findByGithubId(githubId);

        userOpt.ifPresentOrElse(user -> {
            List<String> favorites = user.favorites();
            if (!favorites.contains(movieSlug)) {
                favorites.add(movieSlug);
                userRepo.save(new User(user.id(), user.githubId(), user.username(), favorites));
                System.out.println("Movie added successfully.");
            } else {
                System.out.println("Movie already in watchlist.");
            }
        }, () -> System.out.println("User not found."));
    }

    // ✅ Remove a movie from the watchlist
    public void removeFromWatchlist(String githubId, String movieSlug) {
        Optional<User> userOpt = userRepo.findByGithubId(githubId);

        userOpt.ifPresentOrElse(user -> {
            List<String> favorites = user.favorites();
            if (favorites.contains(movieSlug)) {
                favorites.remove(movieSlug);
                userRepo.save(new User(user.id(), user.githubId(), user.username(), favorites));
                System.out.println("Movie removed successfully.");
            } else {
                System.out.println("Movie not found in watchlist.");
            }
        }, () -> System.out.println("User not found."));
    }
}
