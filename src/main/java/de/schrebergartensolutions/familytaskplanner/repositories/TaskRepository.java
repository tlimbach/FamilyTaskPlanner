package de.schrebergartensolutions.familytaskplanner.repositories;

import de.schrebergartensolutions.familytaskplanner.entities.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TaskRepository extends JpaRepository<Task, Long> {

//    Page<Task> findByKamel_Id(Long kamelId, Pageable pageable);

    long countByKamel_Id(Long kamelId);

    // Zum Ausprobieren
    // Variante A: EntityGraph
    @EntityGraph(attributePaths = {"kamel"})
    Page<Task> findByKamel_Id(Long id, Pageable p);

    // Variante B: JOIN FETCH (inkl. Count-Query!)
    @Query(value = """
      select t from Task t
      join  fetch t.kamel k
      where k.id = :id
      """,
            countQuery = "select count(t) from Task t where t.kamel.id = :id")
    Page<Task> pageByAssigneeWithUser(@Param("id") Long id, Pageable p);
}