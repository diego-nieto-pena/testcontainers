package io.testcontainers.containers;

import io.testcontainers.commons.ElasticsearchTestContainer;
import io.testcontainers.dto.ProductDTO;
import io.testcontainers.model.Product;
import io.testcontainers.service.ProductService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.elasticsearch.ElasticsearchContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ElasticsearchIT {

    @Autowired
    private ProductService productService;

    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Container
    private static ElasticsearchContainer elasticsearchContainer =
            new ElasticsearchTestContainer();

    @BeforeEach
    public void setup() {
        initIndex();
        final ProductDTO product1 = ProductDTO.builder()
                .name("Clock")
                .code("P001")
                .price(120.7)
                .build();

        final ProductDTO product2 = ProductDTO.builder()
                .name("Laptop")
                .code("P002")
                .price(700)
                .build();
        productService.save(product1);
        productService.save(product2);
    }

    @Test
    public void test_get_all_products() {
        final List<ProductDTO> products = productService.getAll();
        assertNotNull(products);
        assertEquals(2, products.size());
    }


    @Test
    public void test_get_product_by_code() {
        final ProductDTO productDTO = testRestTemplate
                .getForObject("/api/v1/products/{code}", ProductDTO.class, "P001");
        assertEquals("Clock", productDTO.getName());
    }

    private void initIndex() {
        if(elasticsearchRestTemplate.exists("products", Product.class)) {
            elasticsearchRestTemplate.delete("products", Product.class);
            elasticsearchRestTemplate.indexOps(Product.class).create();
        }
    }
}
