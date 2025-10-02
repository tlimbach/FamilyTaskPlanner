package de.schrebergartensolutions.familytaskplanner.service;

import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import de.schrebergartensolutions.familytaskplanner.entities.Task;
import de.schrebergartensolutions.familytaskplanner.entities.TaskPrio;
import de.schrebergartensolutions.familytaskplanner.entities.TaskStatus;
import de.schrebergartensolutions.familytaskplanner.repositories.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository repo;

    public Page<Task> pageByAssignee(Long bearbeiterId, Pageable pageable) {
        return repo.findByKamel_Id(bearbeiterId, pageable);
    }

    public long countByAssignee(Long bearbeiterId) {
        return repo.countByKamel_Id(bearbeiterId);
    }

    public Optional<Task> findById(Long id) {
        return repo.findById(id);
    }

    @Transactional
    public Task save(Task task) {
        return repo.save(task);
    }

    @Transactional
    public Task create(String titel, String beschreibung, Benutzer bearbeiter, TaskStatus status, TaskPrio prio, Benutzer ersteller) {
        Task t = new Task();
        t.setTitel(titel);
        t.setBeschreibung(beschreibung);
        t.setKamel(bearbeiter);
        t.setStatus(status);
        t.setPrio(prio);
        t.setKamelTreiber(ersteller);
        return repo.save(t);
    }

    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}