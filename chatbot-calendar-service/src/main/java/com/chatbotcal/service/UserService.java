package com.chatbotcal.service;

import com.chatbotcal.repository.entity.User;
import com.chatbotcal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User getOrCreateUser(
            String userId,
            String firstName,
            String lastName,
            String username) {
        Optional<User> existing = userRepository.findById(userId);
        if (existing.isPresent()) {
            return existing.get();
        }

        User newUser = User.builder()
                .id(userId)
                .firstName(firstName)
                .lastName(lastName)
                .username(username)
                .build();

        return userRepository.save(newUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

}
