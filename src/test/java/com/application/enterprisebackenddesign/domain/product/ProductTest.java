package com.application.enterprisebackenddesign.domain.product;

import com.application.enterprisebackenddesign.domain.shared.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ProductTest {

    private static final Currency USD = Currency.getInstance("USD");

    private Money price(double value) throws DomainException {
        return new Money(BigDecimal.valueOf(value), USD);
    }

    @Test
    void shouldCreateProduct() throws DomainException {
        Product product = new Product(1L, "Widget", price(19.99), "WID-001");
        assertThat(product.getId()).isEqualTo(1L);
        assertThat(product.getName()).isEqualTo("Widget");
        assertThat(product.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("19.99"));
        assertThat(product.getSku()).isEqualTo("WID-001");
    }

    @Test
    void shouldConvertSkuToUpperCase() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "wid-001");
        assertThat(product.getSku()).isEqualTo("WID-001");
    }

    @Test
    void shouldEmitProductCreatedEvent() throws DomainException {
        Product product = new Product(1L, "Widget", price(19.99), "WID-001");
        List<DomainEvent> events = product.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductCreatedEvent.class);
        ProductCreatedEvent event = (ProductCreatedEvent) events.get(0);
        assertThat(event.getName()).isEqualTo("Widget");
        assertThat(event.getSku()).isEqualTo("WID-001");
    }

    @Test
    void shouldRejectNullId() {
        assertThatThrownBy(() -> new Product(null, "Widget", price(10.00), "WID-001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Id cannot be null");
    }

    @Test
    void shouldRejectNullName() {
        assertThatThrownBy(() -> new Product(1L, null, price(10.00), "WID-001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldRejectEmptyName() {
        assertThatThrownBy(() -> new Product(1L, "", price(10.00), "WID-001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Name cannot be null or empty");
    }

    @Test
    void shouldRejectNullPrice() {
        assertThatThrownBy(() -> new Product(1L, "Widget", null, "WID-001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Price cannot be null or zero");
    }

    @Test
    void shouldRejectZeroPrice() {
        assertThatThrownBy(() -> new Product(1L, "Widget", Money.zero(USD), "WID-001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Price cannot be null or zero");
    }

    @Test
    void shouldRejectNullSku() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku cannot be null or empty");
    }

    @Test
    void shouldRejectEmptySku() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), ""))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku cannot be null or empty");
    }

    @Test
    void shouldRejectSkuStartingWithZero() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), "0WID-001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku format is invalid");
    }

    @Test
    void shouldRejectSkuWithAtSymbol() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), "WID@001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku format is invalid");
    }

    @Test
    void shouldRejectSkuWithAmpersand() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), "WID&001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku format is invalid");
    }

    @Test
    void shouldRejectSkuWithLessThan() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), "WID<001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku format is invalid");
    }

    @Test
    void shouldRejectSkuWithGreaterThan() {
        assertThatThrownBy(() -> new Product(1L, "Widget", price(10.00), "WID>001"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("Sku format is invalid");
    }

    @Test
    void shouldUpdatePrice() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        Money newPrice = price(15.00);
        product.updatePrice(newPrice);
        assertThat(product.getPrice().getAmount()).isEqualByComparingTo(new BigDecimal("15.00"));
    }

    @Test
    void shouldEmitProductUpdatedEventOnPriceChange() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        product.updatePrice(price(15.00));
        List<DomainEvent> events = product.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductUpdatedEvent.class);
        ProductUpdatedEvent event = (ProductUpdatedEvent) events.get(0);
        assertThat(event.getFieldName()).isEqualTo("price");
    }

    @Test
    void shouldRejectUpdateToNullPrice() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        assertThatThrownBy(() -> product.updatePrice(null))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("New price cannot be null or zero");
    }

    @Test
    void shouldRejectUpdateToZeroPrice() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        assertThatThrownBy(() -> product.updatePrice(Money.zero(USD)))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("New price cannot be null or zero");
    }

    @Test
    void shouldUpdateName() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        product.updateName("Super Widget");
        assertThat(product.getName()).isEqualTo("Super Widget");
    }

    @Test
    void shouldEmitProductUpdatedEventOnNameChange() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        product.updateName("Super Widget");
        List<DomainEvent> events = product.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductUpdatedEvent.class);
        ProductUpdatedEvent event = (ProductUpdatedEvent) events.get(0);
        assertThat(event.getFieldName()).isEqualTo("name");
    }

    @Test
    void shouldRejectUpdateToEmptyName() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        assertThatThrownBy(() -> product.updateName(""))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("New name cannot be null or empty");
    }

    @Test
    void shouldUpdateSku() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        product.updateSku("WID-002");
        assertThat(product.getSku()).isEqualTo("WID-002");
    }

    @Test
    void shouldUppercaseSkuOnUpdate() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.updateSku("wid-002");
        assertThat(product.getSku()).isEqualTo("WID-002");
    }

    @Test
    void shouldEmitProductUpdatedEventOnSkuChange() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        product.updateSku("WID-002");
        List<DomainEvent> events = product.pullEvents(false);
        assertThat(events).hasSize(1);
        assertThat(events.get(0)).isInstanceOf(ProductUpdatedEvent.class);
        ProductUpdatedEvent event = (ProductUpdatedEvent) events.get(0);
        assertThat(event.getFieldName()).isEqualTo("sku");
    }

    @Test
    void shouldRejectSkuStartingWithZeroOnUpdate() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        assertThatThrownBy(() -> product.updateSku("0WID-002"))
                .isInstanceOf(DomainException.BusinessRuleViolationException.class)
                .hasMessageContaining("New sku format is invalid");
    }

    @Test
    void shouldClearEventsOnPullWithClear() throws DomainException {
        Product product = new Product(1L, "Widget", price(10.00), "WID-001");
        product.pullEvents(true);
        assertThat(product.pullEvents(false)).isEmpty();
    }
}
