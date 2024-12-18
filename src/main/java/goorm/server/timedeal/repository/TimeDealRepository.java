package goorm.server.timedeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;

import goorm.server.timedeal.model.TimeDeal;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long>,
	RevisionRepository<TimeDeal, Long, Integer> {
}