package io.testcontainers.controller;

import io.testcontainers.dto.ProductDTO;
import io.testcontainers.model.Product;
import io.testcontainers.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        try {
            return new ResponseEntity<>(productService.getAll(), HttpStatus.OK);
        } catch(Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody ProductDTO product) {
        try {
            final ProductDTO savedProduct = productService.save(product);
            return new ResponseEntity<>(savedProduct, HttpStatus.CREATED);
        } catch(Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{code}")
    public ResponseEntity<ProductDTO> getProductByCode(@PathVariable String code) {
        try {
            final Optional<ProductDTO> optProduct = productService.getByCode(code);
            return new ResponseEntity<>(optProduct.get(), HttpStatus.OK);
        } catch(Exception ex) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
