package io.testcontainers.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

@Getter
@Setter
@Document(indexName = "products")
public class Product {

    @Id
    private String id;

    private String code;

    private String name;

    private double price;

}
