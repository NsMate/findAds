package com.advertise.service;

import com.advertise.dto.register.RegisterRequest;
import com.advertise.entity.User;
import com.advertise.exception.UserAlreadyExistsException;
import com.advertise.repository.UserRepository;
import com.advertise.security.encoder.PasswordEncoder;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

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
	public User register(RegisterRequest registerRequest) {

		if (userRepository.findByNameEquals(registerRequest.username()).isPresent()) {
			throw new UserAlreadyExistsException("Username '" + registerRequest.username() + "' is already taken");
		}

		if (userRepository.findByEmailEquals(registerRequest.email()).isPresent()) {
			throw new RuntimeException("Email '" + registerRequest.email() + "' is already registered");
		}

		String encodedPassword = passwordEncoder.encode(registerRequest.password());
		User user = new User(null, registerRequest.email(), registerRequest.username(), encodedPassword, Instant.now());
		return userRepository.save(user);
	}
}
