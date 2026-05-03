package com.application.enterprisebackenddesign.api.order;

import com.application.enterprisebackenddesign.application.order.*;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final CreateOrderUseCase createOrderUseCase;
    private final ConfirmOrderUseCase confirmOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;
    private final AddOrderLineUseCase addOrderLineUseCase;
    private final UpdateOrderLineUseCase updateOrderLineUseCase;
    private final RemoveOrderLineUseCase removeOrderLineUseCase;
    private final OrderMapper orderMapper;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           ConfirmOrderUseCase confirmOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase,
                           AddOrderLineUseCase addOrderLineUseCase,
                           UpdateOrderLineUseCase updateOrderLineUseCase,
                           RemoveOrderLineUseCase removeOrderLineUseCase,
                           OrderMapper orderMapper) {
        this.createOrderUseCase = createOrderUseCase;
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.addOrderLineUseCase = addOrderLineUseCase;
        this.updateOrderLineUseCase = updateOrderLineUseCase;
        this.removeOrderLineUseCase = removeOrderLineUseCase;
        this.orderMapper = orderMapper;
    }

    @PostMapping("/")
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) throws DomainException.BusinessRuleViolationException {
        Currency currency = Currency.getInstance(request.currency());

        var lines = request.lines().stream()
                .map(lr -> orderMapper.toOrderLine(UUID.randomUUID().getMostSignificantBits(), lr, currency))
                .collect(Collectors.toList());

        Order order = createOrderUseCase.execute(UUID.randomUUID().getMostSignificantBits(), request.customerId(), lines, currency);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirmOrder(@PathVariable Long id) throws DomainException.BusinessRuleViolationException {
        Order order = confirmOrderUseCase.execute(id);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) throws DomainException.BusinessRuleViolationException {
        Order order = cancelOrderUseCase.execute(id);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/lines")
    public OrderResponse addLine(@PathVariable Long id, @RequestBody OrderLineRequest request) throws DomainException.BusinessRuleViolationException {
        Currency currency = Currency.getInstance("USD");
        Money price = new Money(request.price(), currency);
        Order order = addOrderLineUseCase.execute(id, UUID.randomUUID().getMostSignificantBits(), request.productId(), price, request.quantity());
        return orderMapper.toResponse(order);
    }

    @PutMapping("/{id}/lines/{lineId}")
    public OrderResponse updateLine(@PathVariable Long id, @PathVariable Long lineId, @RequestBody OrderLineRequest request) throws DomainException.BusinessRuleViolationException {
        Order order = updateOrderLineUseCase.execute(id, lineId, request.quantity());
        return orderMapper.toResponse(order);
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    public OrderResponse removeLine(@PathVariable Long id, @PathVariable Long lineId) throws DomainException.BusinessRuleViolationException {
        Order order = removeOrderLineUseCase.execute(id, lineId);
        return orderMapper.toResponse(order);
    }
}
