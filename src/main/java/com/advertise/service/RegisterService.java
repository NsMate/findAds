package com.advertise.service;

import com.advertise.entity.User;
import com.advertise.repository.UserRepository;
import com.advertise.security.encoder.PasswordEncoder;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.time.Instant;

@Singleton
public class RegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void register(@Email String email, @NotBlank String username,
                  @NotBlank String rawPassword) {

        if (userRepository.findByNameEquals(username).isPresent()) {
            throw new RuntimeException("Username '" + username + "' is already taken");
        }

        if (userRepository.findByEmailEquals(email).isPresent()) {
            throw new RuntimeException("Email '" + email + "' is already registered");
        }


        String encodedPassword = passwordEncoder.encode(rawPassword);
        User user = new User(null, email, username, encodedPassword, Instant.now());
        User savedUser = userRepository.save(user);

        System.out.println("Created user: " + email);
    }
}
