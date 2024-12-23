package goorm.server.timedeal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.history.RevisionRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.history.Revision;
import org.springframework.data.history.Revisions;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;

import goorm.server.timedeal.model.Product;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long>,
	RevisionRepository<Product, Long, Integer> {
}
