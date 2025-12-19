package com.advertise;

import com.advertise.service.RegisterService;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

@Singleton
public class Application implements ApplicationEventListener<StartupEvent> {

    private final RegisterService registerService;

    public Application(RegisterService registerService) {
        this.registerService = registerService;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        registerService.register("test@gmail.com", "sherlock", "elementary");
    }

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
