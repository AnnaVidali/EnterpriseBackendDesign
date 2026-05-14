package com.application.enterprisebackenddesign.api.order;

import com.application.enterprisebackenddesign.application.order.*;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.List;
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
    private final GetOrderUseCase getOrderUseCase;
    private final ListOrdersUseCase listOrdersUseCase;
    private final OrderMapper orderMapper;

    public OrderController(CreateOrderUseCase createOrderUseCase,
                           ConfirmOrderUseCase confirmOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase,
                           AddOrderLineUseCase addOrderLineUseCase,
                           UpdateOrderLineUseCase updateOrderLineUseCase,
                           RemoveOrderLineUseCase removeOrderLineUseCase, GetOrderUseCase getOrderUseCase, ListOrdersUseCase listOrdersUseCase,
                           OrderMapper orderMapper) {
        this.createOrderUseCase = createOrderUseCase;
        this.confirmOrderUseCase = confirmOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.addOrderLineUseCase = addOrderLineUseCase;
        this.updateOrderLineUseCase = updateOrderLineUseCase;
        this.removeOrderLineUseCase = removeOrderLineUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.listOrdersUseCase = listOrdersUseCase;
        this.orderMapper = orderMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) throws DomainException {
        Currency currency = Currency.getInstance(request.currency());

        var lines = request.lines().stream()
                .map(lr -> orderMapper.toOrderLine(UUID.randomUUID().getMostSignificantBits(), lr, currency))
                .collect(Collectors.toList());

        Order order = createOrderUseCase.execute(UUID.randomUUID().getMostSignificantBits(), request.customerId(), lines, currency);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/confirm")
    public OrderResponse confirmOrder(@PathVariable Long id) throws DomainException {
        Order order = confirmOrderUseCase.execute(id);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancelOrder(@PathVariable Long id) throws DomainException {
        Order order = cancelOrderUseCase.execute(id);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/lines")
    public OrderResponse addLine(@PathVariable Long id, @RequestBody OrderLineRequest request) throws DomainException {
        Currency currency = Currency.getInstance(request.currency());
        Money price = new Money(request.price(), currency);
        Order order = addOrderLineUseCase.execute(id, UUID.randomUUID().getMostSignificantBits(), request.productId(), price, request.quantity());
        return orderMapper.toResponse(order);
    }

    @PutMapping("/{id}/lines/{lineId}")
    public OrderResponse updateLine(@PathVariable Long id, @PathVariable Long lineId, @RequestBody OrderLineRequest request) throws DomainException {
        Order order = updateOrderLineUseCase.execute(id, lineId, request.quantity());
        return orderMapper.toResponse(order);
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    public OrderResponse removeLine(@PathVariable Long id, @PathVariable Long lineId) throws DomainException {
        Order order = removeOrderLineUseCase.execute(id, lineId);
        return orderMapper.toResponse(order);
    }

    @GetMapping("/{id}")
    public OrderResponse getOrder(@PathVariable Long id) throws DomainException {
        Order order = getOrderUseCase.getOrderById(id);
        return orderMapper.toResponse(order);
    }

    @GetMapping
    public List<OrderResponse> getOrders(@RequestParam(required = false) Long customerId, @RequestParam(required = false) OrderStatus status) {
        List<Order> orders;
        if (customerId != null && status != null) {
            orders = listOrdersUseCase.listByCustomerIdAndStatus(customerId, status);
        } else if (customerId != null) {
            orders = listOrdersUseCase.listByCustomerId(customerId);
        } else if (status != null) {
            orders = listOrdersUseCase.listByStatus(status);
        } else {
            orders = listOrdersUseCase.listAll();
        }
        return orders.stream()
                .map(orderMapper::toResponse)
                .collect(Collectors.toList());
    }
}
