package goorm.server.timedeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import goorm.server.timedeal.model.Purchase;

public interface PurchaseRepository extends JpaRepository<Purchase, Long>{
}