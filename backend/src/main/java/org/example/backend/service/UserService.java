package org.example.backend.service;

import org.example.backend.model.User;
import org.example.backend.repo.UserRepo;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepo userRepo;

    public UserService(UserRepo userRepo) {
        this.userRepo = userRepo;
    }

    public User saveUser(String username, String email) {
        Optional<User> existingUser = userRepo.findByUsername(username);

        if (existingUser.isPresent()) {
            return existingUser.get();
        } else {
            User newUser = new User(null, username, List.of());
            return userRepo.save(newUser);
        }
    }

    public Optional<User> getUser(String username) {
        return userRepo.findByUsername(username);
    }
}
