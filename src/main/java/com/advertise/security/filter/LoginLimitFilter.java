package com.advertise.security.filter;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Filter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Filter("/login")
public class LoginLimitFilter implements HttpServerFilter {

    private final Map<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    @Override
    public Publisher<MutableHttpResponse<?>> doFilter(HttpRequest<?> request, ServerFilterChain chain) {
        String ip = request.getRemoteAddress().getAddress().getHostAddress();
        AttemptInfo info = attempts.computeIfAbsent(ip, _ -> new AttemptInfo());

        if (info.count >= 5 && System.currentTimeMillis() - info.firstAttempt < 300000) {
            return Mono.just(HttpResponse.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many attempts. Try again later."));
        }

        if (System.currentTimeMillis() - info.firstAttempt > 300000) {
            info.reset();
        }

        info.count++;
        return chain.proceed(request);
    }

    private static class AttemptInfo {
        int count = 0;
        long firstAttempt = System.currentTimeMillis();
        void reset() { count = 0; firstAttempt = System.currentTimeMillis(); }
    }
}
