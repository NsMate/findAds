package com.advertise.config;

import io.micronaut.context.annotation.Factory;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.server.cors.CorsOriginConfiguration;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Collections;

@Factory
public class CorsConfig {

	@Singleton
	public CorsOriginConfiguration corsOriginConfiguration() {
		CorsOriginConfiguration config = new CorsOriginConfiguration();

		config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));

		config.setAllowedMethods(
				Arrays.asList(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE, HttpMethod.OPTIONS));

		config.setAllowedHeaders(Arrays.asList("Content-Type", "Authorization"));

		config.setExposedHeaders(Collections.singletonList("Set-Cookie"));

		config.setAllowCredentials(true);

		return config;
	}
}
