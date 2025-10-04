package de.schrebergartensolutions.familytaskplanner.api;

import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import de.schrebergartensolutions.familytaskplanner.service.BenutzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class BenutzerController {

    private final BenutzerService service;

    @GetMapping
    public Page<Benutzer> page(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "50") int size, @RequestParam(required = false) String q) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return service.page(pageable);
    }
}