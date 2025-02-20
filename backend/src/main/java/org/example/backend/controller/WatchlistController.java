package org.example.backend.controller;

import org.example.backend.service.WatchlistService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users/watchlist")
public class WatchlistController {

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    // ✅ Check if a movie is in the watchlist
    @GetMapping("/{githubId}/{movieSlug}")
    public ResponseEntity<Map<String, Boolean>> isMovieInWatchlist(@PathVariable String githubId, @PathVariable String movieSlug) {
        System.out.println("Received request to check watchlist for user " + githubId);
        boolean isInWatchlist = watchlistService.isMovieInWatchlist(githubId, movieSlug);
        return ResponseEntity.ok(Collections.singletonMap("inWatchlist", isInWatchlist));
    }

    // ✅ Add to watchlist
    @PostMapping("/{githubId}/{movieSlug}")
    public ResponseEntity<String> addToWatchlist(@PathVariable String githubId, @PathVariable String movieSlug) {
        System.out.println("Received request to add movie to watchlist for user " + githubId);
        watchlistService.addToWatchlist(githubId, movieSlug);
        return ResponseEntity.ok("Movie added to watchlist.");
    }

    // ✅ Remove from watchlist
    @DeleteMapping("/{githubId}/{movieSlug}")
    public ResponseEntity<String> removeFromWatchlist(@PathVariable String githubId, @PathVariable String movieSlug) {
        System.out.println("Received request to remove movie from watchlist for user " + githubId);
        watchlistService.removeFromWatchlist(githubId, movieSlug);
        return ResponseEntity.ok("Movie removed from watchlist.");
    }
}
