package com.hiretrack.user;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User register(String email, String rawPassword, String fullName) {
        // Rule: no duplicate emails (repository supplies the fact, service decides)
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }

        // Never store the raw password — hash it first
        String hashedPassword = passwordEncoder.encode(rawPassword);

        User user = new User(email, hashedPassword, fullName);
        return userRepository.save(user);
    }
}