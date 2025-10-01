package de.schrebergartensolutions.familytaskplanner.entities;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Objects;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of = "id")   // <— equals/hashCode nur über die ID
public class Benutzer {

    private final String id = UUID.randomUUID().toString();

    private String name;

    private String farbe;


}

