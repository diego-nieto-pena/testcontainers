package io.testcontainers.service;

import io.testcontainers.dto.ProductDTO;
import io.testcontainers.exception.ProductNotFoundException;
import io.testcontainers.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<ProductDTO> getAll();

    Optional<ProductDTO> getByCode(String code) throws ProductNotFoundException;

    ProductDTO save(ProductDTO product);

    void deleteById(String id);
}
