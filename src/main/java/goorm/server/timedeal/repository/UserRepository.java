package goorm.server.timedeal.repository;

import org.springframework.data.history.Revision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import java.util.Optional;

import goorm.server.timedeal.model.User;

public interface UserRepository extends JpaRepository<User, Long>,
	RevisionRepository<User, Long, Long> {
}
