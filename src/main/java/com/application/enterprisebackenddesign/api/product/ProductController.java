package com.application.enterprisebackenddesign.api.product;

import com.application.enterprisebackenddesign.application.product.*;
import com.application.enterprisebackenddesign.domain.product.Product;
import com.application.enterprisebackenddesign.domain.shared.DomainException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final CreateProductUseCase createProductUseCase;
    private final DeleteProductUseCase deleteProductUseCase;
    private final GetProductUseCase getProductUseCase;
    private final ListProductsUseCase listProductsUseCase;
    private final UpdateProductUseCase updateProductUseCase;
    private final ProductMapper productMapper;

    public ProductController(CreateProductUseCase createProductUseCase, DeleteProductUseCase deleteProductUseCase, GetProductUseCase getProductUseCase, ListProductsUseCase listProductsUseCase, UpdateProductUseCase updateProductUseCase, ProductMapper productMapper) {
        this.createProductUseCase = createProductUseCase;
        this.deleteProductUseCase = deleteProductUseCase;
        this.getProductUseCase = getProductUseCase;
        this.listProductsUseCase = listProductsUseCase;
        this.updateProductUseCase = updateProductUseCase;
        this.productMapper = productMapper;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@RequestBody @Valid ProductRequest productRequest) throws DomainException {
        Product product = createProductUseCase.create(UUID.randomUUID().getMostSignificantBits(), productRequest);
        return productMapper.toResponse(product);
    }

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable Long id) throws DomainException {
        Product product = getProductUseCase.getProductById(id);
        return productMapper.toResponse(product);
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        List<Product> products = listProductsUseCase.listAll();
        return products.stream()
                .map(productMapper::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable Long id, @RequestBody @Valid ProductRequest productRequest) throws DomainException {
        Product product = updateProductUseCase.update(id, productRequest);
        return productMapper.toResponse(product);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable Long id) throws DomainException {
        deleteProductUseCase.delete(id);
    }
}
