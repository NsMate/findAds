package com.advertise;

import com.advertise.dto.register.RegisterRequest;
import com.advertise.entity.User;
import com.advertise.repository.UserRepository;
import com.advertise.service.RegisterService;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

	private final RegisterService registerService;
	private final UserRepository userRepository;

	public Application(RegisterService registerService, UserRepository userRepository) {
		this.registerService = registerService;
		this.userRepository = userRepository;
	}

	@Override
	public void onApplicationEvent(StartupEvent event) {
		if (userRepository.count() == 0) {
			RegisterRequest registerRequest = new RegisterRequest("sherlock", "test@gmail.com", "elementary");
			User _ = registerService.register(registerRequest);
		}
	}

	public static void main(String[] args) {
		Micronaut.run(Application.class, args);
	}
}
