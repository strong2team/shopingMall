package goorm.server.timedeal.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test Controller for TimeDeal Project.
 * Provides health check & dummy endpoints for testing.
 */
@RestController
@RequestMapping("/api/test")
public class TestController {

	@GetMapping("/hello")
	public String sayHello() {
		return "Hello, Goorm Team2 TimeDeal!";
	}

	@GetMapping("/hello2")
	public String sayHello2() {
		return "Hello, Goorm Team2 TimeDeal Version2!";
	}


	@GetMapping("/status")
	public String statusCheck() {
		return "TimeDeal Project is up and running!";
	}


}
