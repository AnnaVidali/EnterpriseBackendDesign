package com.application.enterprisebackenddesign.infrastructure.persistence.product;

import com.application.enterprisebackenddesign.TestcontainersConfiguration;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
@Sql(statements = "DELETE FROM products", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class ProductRepositoryImplIntegrationTest {

    @Autowired
    private ProductRepositoryImpl productRepository;

    private Money usd(double amount) throws Exception {
        return new Money(BigDecimal.valueOf(amount), Currency.getInstance("USD"));
    }

    @Test
    void shouldSaveAndFindById() throws Exception {
        Product product = new Product(1L, "Widget", usd(10), "WID-001");
        Product saved = productRepository.save(product);

        assertThat(saved.getId()).isEqualTo(1L);
        assertThat(saved.getName()).isEqualTo("Widget");
        assertThat(saved.getSku()).isEqualTo("WID-001");

        Optional<Product> found = productRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Widget");
        assertThat(found.get().getSku()).isEqualTo("WID-001");
        assertThat(found.get().getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("10.00"));
    }

    @Test
    void shouldFindBySku() throws Exception {
        productRepository.save(new Product(1L, "Widget", usd(10), "WID-001"));

        Optional<Product> found = productRepository.findBySku("WID-001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Widget");
    }

    @Test
    void shouldReturnEmptyWhenSkuNotFound() {
        Optional<Product> found = productRepository.findBySku("NONEXISTENT");
        assertThat(found).isEmpty();
    }

    @Test
    void shouldFindAll() throws Exception {
        productRepository.save(new Product(1L, "Widget", usd(10), "WID-001"));
        productRepository.save(new Product(2L, "Gadget", usd(20), "GDG-001"));

        var products = productRepository.findAll();
        assertThat(products).hasSize(2);
    }

    @Test
    void shouldFindAllWithPagination() throws Exception {
        productRepository.save(new Product(1L, "A", usd(10), "SKU-001"));
        productRepository.save(new Product(2L, "B", usd(20), "SKU-002"));
        productRepository.save(new Product(3L, "C", usd(30), "SKU-003"));

        Page<Product> page = productRepository.findAll(PageRequest.of(0, 2));
        assertThat(page.getContent()).hasSize(2);
        assertThat(page.getTotalElements()).isEqualTo(3);
    }

    @Test
    void shouldUpdate() throws Exception {
        productRepository.save(new Product(1L, "Widget", usd(10), "WID-001"));

        Product updated = new Product(1L, "Widget Pro", usd(25), "WID-PRO");
        productRepository.save(updated);

        Optional<Product> found = productRepository.findById(1L);
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Widget Pro");
        assertThat(found.get().getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(found.get().getSku()).isEqualTo("WID-PRO");
    }

    @Test
    void shouldDelete() throws Exception {
        productRepository.save(new Product(1L, "Widget", usd(10), "WID-001"));
        productRepository.delete(new Product(1L, "Widget", usd(10), "WID-001"));

        Optional<Product> found = productRepository.findById(1L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
        Optional<Product> found = productRepository.findById(9999L);
        assertThat(found).isEmpty();
    }

    @Test
    void shouldRejectInvalidProduct() {
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Product(1L, "", usd(10), "SKU-001"));
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Product(1L, "Name", usd(0), "SKU-001"));
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Product(1L, "Name", usd(10), ""));
        assertThrows(DomainException.BusinessRuleViolationException.class,
                () -> new Product(1L, "Name", usd(10), "0invalid"));
    }
}
