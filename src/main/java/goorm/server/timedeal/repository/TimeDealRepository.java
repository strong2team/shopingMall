package goorm.server.timedeal.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;

import goorm.server.timedeal.model.TimeDeal;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long>,
	RevisionRepository<TimeDeal, Long, Integer> {

	// ACTIVE 및 SCHEDULED 상태 타임딜 가져오기
	@Query("SELECT t FROM TimeDeal t WHERE t.status = 'ACTIVE' OR t.status = 'SCHEDULED'")
	List<TimeDeal> findActiveAndScheduledDeals();
}