package de.schrebergartensolutions.familytaskplanner.repositories;

import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BenutzerRepository extends JpaRepository<Benutzer, Long> {
    boolean existsByName(String name);
    Page<Benutzer> findAll(Pageable pageable);
}
