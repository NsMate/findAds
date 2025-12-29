package com.advertise.controller;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.security.Principal;

@Secured(SecurityRule.IS_AUTHENTICATED)
@Controller("/hello")
public class HelloController {

	@Get(produces = MediaType.TEXT_PLAIN)
	public String hello(Principal principal) {
		System.out.println(principal.getName());
		return "Hello World";
	}
}
