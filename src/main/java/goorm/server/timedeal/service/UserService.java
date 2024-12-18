package goorm.server.timedeal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import goorm.server.timedeal.model.User;
import goorm.server.timedeal.repository.UserRepository;
import jakarta.transaction.Transactional;

@Service
public class UserService {

	@Autowired
	private UserRepository userRepository;

	// User 생성
	@Transactional
	public User createUser(User user) {
		return userRepository.save(user);
	}

	// User 수정
	@Transactional
	public User updateUser(int id, User updatedUser) {
		return userRepository.findById(id)
			.map(user -> {
				user.setUsername(updatedUser.getUsername());
				user.setPassword(updatedUser.getPassword());
				user.setEmail(updatedUser.getEmail());
				user.setRole(updatedUser.getRole());
				return userRepository.save(user);
			}).orElseThrow(() -> new RuntimeException("User not found with id: " + id));
	}
}
