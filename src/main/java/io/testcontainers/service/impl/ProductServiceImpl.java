package io.testcontainers.service.impl;

import io.testcontainers.dto.ProductDTO;
import io.testcontainers.exception.ProductNotFoundException;
import io.testcontainers.mapper.ProductMapper;
import io.testcontainers.model.Product;
import io.testcontainers.repository.ProductRepository;
import io.testcontainers.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    private final ProductMapper mapper = Mappers.getMapper(ProductMapper.class);

    @Override
    public List<ProductDTO> getAll() {
        final Iterable<Product> iterable = productRepository.findAll();
        final List<ProductDTO> products = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterable.iterator(),
                        Spliterator.ORDERED), false)
                .map(mapper::entityToDTO)
                .collect(Collectors.toList());
        return products;
    }

    @Override
    public Optional<ProductDTO> getByCode(String code) throws ProductNotFoundException {
        final Optional<Product> optProduct = productRepository.findByCode(code);
        final Product product = optProduct.orElseThrow(ProductNotFoundException::new);
        return Optional.of(mapper.entityToDTO(product));
    }

    @Override
    public ProductDTO save(ProductDTO product) {
        final Product entity = mapper.dtoToEntity(product);
        return mapper.entityToDTO(productRepository.save(entity));
    }

    @Override
    public void deleteById(String id) {
        productRepository.deleteById(id);
    }
}
