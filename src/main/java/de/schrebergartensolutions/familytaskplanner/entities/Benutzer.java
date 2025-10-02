package de.schrebergartensolutions.familytaskplanner.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "benutzer")
@EqualsAndHashCode(of = "id")   // <— equals/hashCode nur über die ID
public class Benutzer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // DB-ID

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String farbe;

    public Benutzer(String name, String farbe) {
        this.name = name;
        this.farbe = farbe;
    }
}

