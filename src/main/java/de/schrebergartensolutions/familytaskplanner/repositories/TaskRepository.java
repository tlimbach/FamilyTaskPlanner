package de.schrebergartensolutions.familytaskplanner.repositories;

import de.schrebergartensolutions.familytaskplanner.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TaskRepository extends JpaRepository<Task, Long> {

    Page<Task> findByKamel_Id(Long kamelId, Pageable pageable);

    long countByKamel_Id(Long kamelId);

}