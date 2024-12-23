package goorm.server.timedeal.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import goorm.server.timedeal.model.User;
import goorm.server.timedeal.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	@Autowired
	private UserService userService;

	// User 생성 API
	@PostMapping
	public ResponseEntity<User> createUser(@Validated @RequestBody User user) {
		User createdUser = userService.createUser(user);
		return ResponseEntity.ok(createdUser);
	}

	// User 수정 API
	@PutMapping("/{id}")
	public ResponseEntity<User> updateUser(@PathVariable Long id, @Validated @RequestBody User updatedUser) {
		User user = userService.updateUser(id, updatedUser);
		return ResponseEntity.ok(user);
	}
}
