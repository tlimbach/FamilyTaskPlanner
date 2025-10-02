package de.schrebergartensolutions.familytaskplanner.entities;

import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "task")
@EqualsAndHashCode(of = "id")   // <— equals/hashCode nur über die ID
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;          // DB-ID

    @Column(nullable = false, unique = true)
    private String titel;

    @Column(nullable = false)
    private String beschreibung;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kamel_id", nullable = false)
    private Benutzer kamel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kamel_treiber_id", nullable = true)
    private Benutzer kamelTreiber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Column(nullable = false)
    private Timestamp timestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPrio prio;

}

