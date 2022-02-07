package io.testcontainers.service;

import io.testcontainers.dto.PersonDTO;
import io.testcontainers.entity.Person;
import io.testcontainers.mapper.PersonMapper;
import io.testcontainers.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class PersonServiceImpl implements PersonService {

    private PersonMapper mapper = Mappers.getMapper(PersonMapper.class);

    private final PersonRepository personRepository;

    @Override
    public List<PersonDTO> getAll() {
        final List<Person> entityList = personRepository.findAll();
        final List<PersonDTO> dtoList = entityList.stream()
                .map(mapper::entityToDTO)
                .collect(Collectors.toList());

        return dtoList;
    }

    @PostConstruct
    public void init() {
        personRepository.deleteAll();
        personRepository.save(Person.builder().name("Vladimir").build());
        personRepository.save(Person.builder().name("Joe").build());
        personRepository.save(Person.builder().name("Kim").build());
        personRepository.save(Person.builder().name("Angela").build());
        personRepository.save(Person.builder().name("Deimos").build());
    }
}
