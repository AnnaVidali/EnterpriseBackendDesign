package com.application.enterprisebackenddesign.api.product;

import com.application.enterprisebackenddesign.api.shared.PageResponse;
import com.application.enterprisebackenddesign.application.product.*;
import com.application.enterprisebackenddesign.application.shared.IdGenerator;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for the Product aggregate.
 *
 * Hexagonal Architecture (Adapter layer): Inbound adapter that delegates
 * to product use cases. This controller has an interesting security
 * distinction: GET endpoints are public (no authentication required per
 * SecurityConfig), while POST/PUT/DELETE require authentication. This
 * reflects a real-world pattern where the product catalog is publicly
 * readable but only admins can modify it.
 *
 * The controller is intentionally thin — every method is a one-liner
 * that delegates to a use case and maps the result. No business logic
 * lives here.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "CRUD operations for product catalog management")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductMapper productMapper;
    private final IdGenerator idGenerator;

    public ProductController(CreateProductUseCase createProductUseCase, DeleteProductUseCase deleteProductUseCase, GetProductUseCase getProductUseCase, ListProductsUseCase listProductsUseCase, UpdateProductUseCase updateProductUseCase, ProductMapper productMapper, IdGenerator idGenerator) {
        this.createProductUseCase = createProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.productMapper = productMapper;
        this.idGenerator = idGenerator;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product",
            description = "Creates a product with name, price, and SKU. The SKU must be unique.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Product created",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request body or duplicate SKU")
    })
    public ProductResponse createProduct(@RequestBody @Valid ProductRequest productRequest) throws DomainException {
        Product product = createProductUseCase.create(idGenerator.generateId(), productRequest);
        return productMapper.toResponse(product);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a product by ID",
            description = "Returns product details including name, price, and SKU.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product found",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse getProduct(@PathVariable Long id) throws DomainException {
        Product product = getProductUseCase.getProductById(id);
        return productMapper.toResponse(product);
    }

    @GetMapping
    @Operation(summary = "List all products",
            description = "Returns a paginated list of all products in the catalog. This endpoint is public (no authentication required).")
    @ApiResponse(responseCode = "200", description = "Paginated list of products",
            content = @Content(schema = @Schema(implementation = PageResponse.class)))
    public PageResponse<ProductResponse> getAllProducts(
            @Parameter(description = "Pagination and sorting") @PageableDefault(size = 20, sort = "id") Pageable pageable) {
        var productsPage = listProductsUseCase.listAll(pageable);
        return PageResponse.from(productsPage, productsPage.map(productMapper::toResponse).getContent());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a product",
            description = "Updates the name, price, and SKU of an existing product.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Product updated",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "400", description = "Invalid request body or duplicate SKU")
    })
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody @Valid ProductRequest productRequest) throws DomainException {
        Product product = updateProductUseCase.update(id, productRequest);
        return productMapper.toResponse(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product",
            description = "Permanently removes a product from the catalog.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Product deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public void deleteProduct(@PathVariable Long id) throws DomainException {
        deleteProductUseCase.delete(id);
    }
}
