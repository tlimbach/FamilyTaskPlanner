// src/main/java/de/schrebergartensolutions/familytaskplanner/api/BenutzerController.java
package de.schrebergartensolutions.familytaskplanner.api;

import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import de.schrebergartensolutions.familytaskplanner.service.BenutzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class BenutzerController {

    private final BenutzerService service;

    @GetMapping
    public PageResponse<Benutzer> page(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false) String q
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var p = service.page(q, pageable);
        return new PageResponse<>(
                p.getContent(),
                p.getTotalElements(),
                p.getTotalPages(),
                p.getNumber(),
                p.getSize()
        );
    }
}