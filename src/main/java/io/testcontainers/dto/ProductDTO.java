package io.testcontainers.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
public class ProductDTO {
    private String id;

    private String code;

    private String name;

    private double price;
}
