package io.testcontainers.service;

import io.testcontainers.dto.PersonDTO;

import java.util.List;

public interface PersonService {

    List<PersonDTO> getAll();
}
