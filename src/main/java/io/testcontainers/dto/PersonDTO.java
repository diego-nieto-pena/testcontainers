package io.testcontainers.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter@Setter
@ToString
public class PersonDTO {

    private Long id;

    private String name;
}