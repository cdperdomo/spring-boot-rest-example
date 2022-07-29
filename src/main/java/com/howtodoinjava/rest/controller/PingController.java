package com.howtodoinjava.rest.controller;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/log")
public class PingController {
	
	private static final Logger LOG = LoggerFactory.getLogger(PingController.class);
	
	@GetMapping(path = "/info", produces = "application/json")
	public String printInfoLog() {
		LOG.info("Info log {}", LocalDateTime.now());
		return "ok";
	}
	
	@GetMapping(path = "/error", produces = "application/json")
	public String printErrorLog() {
		LOG.info("Error log {}", LocalDateTime.now());
		return "ok";
	}

}
