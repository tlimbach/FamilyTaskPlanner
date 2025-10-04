package de.schrebergartensolutions.familytaskplanner.api;

import de.schrebergartensolutions.familytaskplanner.entities.Task;
import de.schrebergartensolutions.familytaskplanner.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/de/schrebergartensolutions/familytaskplanner/api/tasks")
@RequiredArgsConstructor
public class TaskController {

  private final TaskService taskService;

  @GetMapping
  public Page<Task> page(
      @RequestParam Long assigneeId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "20") int size) {
    var sort = Sort.by(Sort.Order.desc("prio"), Sort.Order.desc("status"), Sort.Order.asc("titel"));
    return taskService.pageByAssignee(assigneeId, PageRequest.of(page, size, sort));
  }

  @PostMapping
  public Task create(@RequestBody Task task) {
    return taskService.save(task);
  }

  @PutMapping("/{id}")
  public Task update(@PathVariable Long id, @RequestBody Task task) {
    task.setId(id);
    return taskService.save(task);
  }

  @DeleteMapping("/{id}")
  public void delete(@PathVariable Long id) {
    taskService.delete(id);
  }
}