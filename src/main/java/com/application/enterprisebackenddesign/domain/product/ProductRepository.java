package com.application.enterprisebackenddesign.domain.product;

import com.application.enterprisebackenddesign.domain.shared.DomainException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for the Product aggregate.
 *
 * Hexagonal Architecture (Port): ProductRepository is the simplest repository
 * in the system — products are standalone aggregates with no child entities.
 * The findBySku method is notable: it enforces SKU uniqueness as a domain
 * invariant, implemented at the persistence level via a unique constraint.
 *
 * DDD note: save() throws DomainException to signal business rule violations
 * (e.g., duplicate SKU at the database level). The implementation catches
 * DataIntegrityViolationException and re-throws as a domain exception.
 */
public interface ProductRepository {

    List<Product> findAll();

    Page<Product> findAll(Pageable pageable);

    Optional<Product> findById(Long id);

    Product save(Product product) throws DomainException;

    Optional<Product> findBySku(String sku);

    void delete(Product product);
}
