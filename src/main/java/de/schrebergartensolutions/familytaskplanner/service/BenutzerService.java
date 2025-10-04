package de.schrebergartensolutions.familytaskplanner.service;

import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import de.schrebergartensolutions.familytaskplanner.repositories.BenutzerRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BenutzerService {
    private final BenutzerRepository repo;

    BenutzerService(BenutzerRepository repo) {
        this.repo = repo;
    }

    public Page<Benutzer> page(Pageable pageable) {
        return repo.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public long count() {
        return repo.count();
    }

    @Transactional
    public Benutzer create(String name, String farbe) {
        if (repo.existsByName(name)) throw new IllegalArgumentException("Name existiert bereits");
        return repo.save(new Benutzer(name, farbe));
    }

    @Transactional
    public Benutzer update(Long id, String name, String farbe) {
        var b = repo.findById(id).orElseThrow();
        b.setName(name);
        b.setFarbe(farbe);
        return b; // Dirty checking
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }

    public Benutzer save(Benutzer b) { return repo.save(b); }
    public boolean existsByName(String name) { return repo.existsByName(name); }

    public List<Benutzer> findAll(Sort name) {
        return repo.findAll(name);
    }

    @Transactional
    public void saveAll(List<Benutzer> users) {
        repo.saveAll(users); // Hibernate batcht das – mit obiger Konfig
    }
}