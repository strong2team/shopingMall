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

}
