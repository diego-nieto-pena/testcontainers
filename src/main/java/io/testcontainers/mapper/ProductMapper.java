package io.testcontainers.mapper;

import io.testcontainers.dto.ProductDTO;
import io.testcontainers.model.Product;
import org.mapstruct.Mapper;

@Mapper
public interface ProductMapper {
    Product dtoToEntity(ProductDTO dto);

    ProductDTO entityToDTO(Product entity);
}
