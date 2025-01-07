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
		//System.out.println("hello에 접속했습니다.");
		return "Hello, Goorm Team2 TimeDeal!";
	}

	@GetMapping("/hello3")
	public String sayHello3() {
		System.out.println("hello3 에 접속했습니다.");
		return "Hello, Goorm Team2 TimeDeal Version3!";
	}
	@GetMapping("/hello4")
	public String sayHello4() {
		System.out.println("hello4 에 접속했습니다.");

		return "Hello, 1/7 hello4() update!After 5:00.";
	}


	@GetMapping("/status")
	public String statusCheck() {
		return "TimeDeal Project is up and running!";
	}


}
