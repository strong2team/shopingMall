package goorm.server.timedeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goorm.server.timedeal.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
