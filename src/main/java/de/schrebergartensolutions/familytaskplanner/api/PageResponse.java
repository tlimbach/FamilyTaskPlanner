// src/main/java/de/schrebergartensolutions/familytaskplanner/api/PageResponse.java
package de.schrebergartensolutions.familytaskplanner.api;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int number,
        int size
) {}