package org.example.service;

import org.example.entity.Olympiad;
import org.example.exception.OlympiadNotFoundException;
import org.example.repository.OlympiadRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class OlympiadService {
    private final OlympiadRepository olympiadRepository;

    public OlympiadService(OlympiadRepository olympiadRepository) {
        this.olympiadRepository = olympiadRepository;
    }

    public Olympiad create(String name, LocalDate date, String description) {
        if (olympiadRepository.findByName(name).isPresent()) {
            throw new RuntimeException("Olympiad with name " + name + " already exists");
        }
        Olympiad olympiad = new Olympiad();
        olympiad.setName(name);
        olympiad.setDate(date);
        olympiad.setDescription(description); // Добавлено сохранение описания
        return olympiadRepository.save(olympiad);
    }

    public void deleteByName(String name) {
        Optional<Olympiad> opt = olympiadRepository.findByName(name);
        if (opt.isEmpty()) {
            throw new OlympiadNotFoundException("Olympiad not found with name: " + name);
        }
        Olympiad olympiad = opt.get();
        olympiadRepository.deleteRelationsByOlympiadId(olympiad.getId());
        olympiadRepository.delete(olympiad);
    }

    public List<Olympiad> getAll() {
        return olympiadRepository.findAll();
    }

    public Optional<Olympiad> findByName(String name) {
        return olympiadRepository.findByName(name);
    }
}