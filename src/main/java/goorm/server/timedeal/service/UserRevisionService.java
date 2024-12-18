package goorm.server.timedeal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.history.Revision;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import goorm.server.timedeal.model.User;
import goorm.server.timedeal.repository.UserRepository;

@Service
public class UserRevisionService {

	@Autowired
	private UserRepository userRepository;

	// 모든 리비전 조회
	public List<Revision<Integer, User>> getUserRevisions(int userId) {
		return userRepository.findRevisions(userId).getContent();
	}

	// 특정 리비전 조회
	public Revision<Integer, User> getUserRevisionByNumber(int userId, int revisionNumber) {
		return userRepository.findRevision(userId, revisionNumber)
			.orElseThrow(() -> new RuntimeException("No revision found for user id: " + userId));
	}
}
