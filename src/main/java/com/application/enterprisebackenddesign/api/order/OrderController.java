package com.application.enterprisebackenddesign.api.order;

import com.application.enterprisebackenddesign.api.shared.PageResponse;
import com.application.enterprisebackenddesign.application.order.*;
import com.application.enterprisebackenddesign.domain.order.Order;
import com.application.enterprisebackenddesign.domain.order.OrderStatus;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import com.application.enterprisebackenddesign.domain.shared.Money;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Currency;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for the Order aggregate — the richest controller in the system.
 *
 * Hexagonal Architecture (Adapter layer): This controller is an inbound adapter
 * that translates HTTP requests into use case calls and domain responses back
 * into HTTP responses. It contains zero business logic — every operation
 * delegates to a dedicated use case.
 *
 * The Order aggregate is the most complex domain object (it owns OrderLine children,
 * has a lifecycle with status transitions, and holds a Money total). This controller
 * demonstrates the full pattern: 8 endpoints covering CRUD, status transitions
 * (confirm/cancel), and child entity management (add/update/remove lines).
 *
 * DDD principle: The controller works only with primitive/DTO types and never
 * manipulates domain objects directly. The use cases handle all domain logic.
 */
@RestController
@RequestMapping("/api/orders")
@Tag(name = "Orders", description = "Order lifecycle management — create, confirm, cancel, and manage order lines")
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
    @Operation(summary = "Create a new order",
            description = "Creates an order with multiple order lines. Each line specifies a product, quantity, and price.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Order created",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public OrderResponse createOrder(@RequestBody CreateOrderRequest request) throws DomainException {
        Currency currency = Currency.getInstance(request.currency());

        var lines = request.lines().stream()
                .map(lr -> orderMapper.toOrderLine(UUID.randomUUID().getMostSignificantBits(), lr, currency))
                .collect(Collectors.toList());

        Order order = createOrderUseCase.execute(UUID.randomUUID().getMostSignificantBits(), request.customerId(), lines, currency);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm an order",
            description = "Transitions the order from PENDING to CONFIRMED status. Triggers invoice creation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order confirmed",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Order is not in PENDING status")
    })
    public OrderResponse confirmOrder(@PathVariable Long id) throws DomainException {
        Order order = confirmOrderUseCase.execute(id);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel an order",
            description = "Transitions the order from PENDING or CONFIRMED to CANCELLED status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order cancelled",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Order cannot be cancelled in its current status")
    })
    public OrderResponse cancelOrder(@PathVariable Long id) throws DomainException {
        Order order = cancelOrderUseCase.execute(id);
        return orderMapper.toResponse(order);
    }

    @PostMapping("/{id}/lines")
    @Operation(summary = "Add a line to an order",
            description = "Adds a new product line to an existing PENDING order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line added",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found"),
            @ApiResponse(responseCode = "400", description = "Order is not in PENDING status")
    })
    public OrderResponse addLine(@PathVariable Long id, @RequestBody OrderLineRequest request) throws DomainException {
        Currency currency = Currency.getInstance(request.currency());
        Money price = new Money(request.price(), currency);
        Order order = addOrderLineUseCase.execute(id, UUID.randomUUID().getMostSignificantBits(), request.productId(), price, request.quantity());
        return orderMapper.toResponse(order);
    }

    @PutMapping("/{id}/lines/{lineId}")
    @Operation(summary = "Update an order line",
            description = "Changes the quantity of an existing order line.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line updated",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order or line not found"),
            @ApiResponse(responseCode = "400", description = "Order is not in PENDING status")
    })
    public OrderResponse updateLine(@PathVariable Long id, @PathVariable Long lineId, @RequestBody OrderLineRequest request) throws DomainException {
        Order order = updateOrderLineUseCase.execute(id, lineId, request.quantity());
        return orderMapper.toResponse(order);
    }

    @DeleteMapping("/{id}/lines/{lineId}")
    @Operation(summary = "Remove an order line",
            description = "Removes a product line from an existing PENDING order.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line removed",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order or line not found"),
            @ApiResponse(responseCode = "400", description = "Order is not in PENDING status")
    })
    public OrderResponse removeLine(@PathVariable Long id, @PathVariable Long lineId) throws DomainException {
        Order order = removeOrderLineUseCase.execute(id, lineId);
        return orderMapper.toResponse(order);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an order by ID",
            description = "Returns the full order with all its lines.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order found",
                    content = @Content(schema = @Schema(implementation = OrderResponse.class))),
            @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public OrderResponse getOrder(@PathVariable Long id) throws DomainException {
        Order order = getOrderUseCase.getOrderById(id);
        return orderMapper.toResponse(order);
    }

    @GetMapping
    @Operation(summary = "List orders",
            description = "Returns a paginated list of orders optionally filtered by customer ID and/or status.")
    @ApiResponse(responseCode = "200", description = "Paginated list of orders",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public PageResponse<OrderResponse> getOrders(
            @Parameter(description = "Filter by customer ID") @RequestParam(required = false) Long customerId,
            @Parameter(description = "Filter by order status") @RequestParam(required = false) OrderStatus status,
            @Parameter(description = "Pagination and sorting") @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        org.springframework.data.domain.Page<Order> ordersPage;
        if (customerId != null && status != null) {
            ordersPage = listOrdersUseCase.listByCustomerIdAndStatus(customerId, status, pageable);
        } else if (customerId != null) {
            ordersPage = listOrdersUseCase.listByCustomerId(customerId, pageable);
        } else if (status != null) {
            ordersPage = listOrdersUseCase.listByStatus(status, pageable);
        } else {
            ordersPage = listOrdersUseCase.listAll(pageable);
        }
        return PageResponse.from(ordersPage, ordersPage.map(orderMapper::toResponse).getContent());
    }
}
