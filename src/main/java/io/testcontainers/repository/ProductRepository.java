package io.testcontainers.repository;

import io.testcontainers.model.Product;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    Optional<Product> findByCode(String code);
}
