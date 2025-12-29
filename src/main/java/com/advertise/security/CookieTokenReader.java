package com.advertise.security;

import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.security.token.reader.HttpHeaderTokenReader;
import io.micronaut.security.token.reader.TokenReader;
import jakarta.inject.Singleton;

import java.util.Optional;

@Singleton
@Replaces(HttpHeaderTokenReader.class)
public class CookieTokenReader implements TokenReader<HttpRequest<?>> {

	@Override
	public Optional<String> findToken(HttpRequest<?> request) {
		Optional<String> authHeader = request.getHeaders().findFirst("Authorization")
				.filter(header -> header.startsWith("Bearer ")).map(header -> header.substring(7));

		if (authHeader.isPresent()) {
			return authHeader;
		}

		return request.getCookies().findCookie("access_token").map(Cookie::getValue);
	}
}
