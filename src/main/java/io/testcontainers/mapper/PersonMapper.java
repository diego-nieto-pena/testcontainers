package io.testcontainers.mapper;

import io.testcontainers.dto.PersonDTO;
import io.testcontainers.entity.Person;
import org.mapstruct.Mapper;

@Mapper
public interface PersonMapper {

    Person dtoToEntity(PersonDTO dto);

    PersonDTO entityToDTO(Person entity);
}