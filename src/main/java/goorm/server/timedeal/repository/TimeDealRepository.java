package goorm.server.timedeal.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.data.repository.query.Param;

import goorm.server.timedeal.model.TimeDeal;
import jakarta.persistence.LockModeType;

public interface TimeDealRepository extends JpaRepository<TimeDeal, Long>,
	RevisionRepository<TimeDeal, Long, Integer> {

	// ACTIVE 및 SCHEDULED 상태 타임딜 가져오기
	@Query("SELECT t FROM TimeDeal t WHERE t.status = 'ACTIVE' OR t.status = 'SCHEDULED'")
	List<TimeDeal> findActiveAndScheduledDeals();

	Optional<TimeDeal> findByProduct_ProductId(Long productId);

	// 타임딜 재고감소. (비관적 락)
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT t FROM TimeDeal t WHERE t.timeDealId = :timeDealId")
	Optional<TimeDeal> findByIdWithLock(@Param("timeDealId") Long timeDealId);


}