package de.schrebergartensolutions.familytaskplanner.repositories;

import de.schrebergartensolutions.familytaskplanner.entities.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class TaskRepositoryFetchTest {

    @Container
    static PostgreSQLContainer<?> pg = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void pgProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url",       pg::getJdbcUrl);
        r.add("spring.datasource.username",  pg::getUsername);
        r.add("spring.datasource.password",  pg::getPassword);
        r.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        r.add("spring.jpa.show-sql", () -> "true");
    }

    @Autowired EntityManager em;
    @Autowired TaskRepository taskRepo;
    @Autowired BenutzerRepository benutzerRepo;

    Long mayaId;

    @BeforeEach
    @Transactional
    void seed() {
        var maya = benutzerRepo.save(new Benutzer("Maya", "#1976d2"));
        mayaId = maya.getId();

        taskRepo.save(new Task(
                null,
                "Energy trinken",
                "Zum Netto …",
                maya,               // kamel
                maya,               // kamelTreiber
                TaskStatus.IN_ARBEIT,
                new java.sql.Timestamp(System.currentTimeMillis()),
                TaskPrio.NIEDRIG
        ));

        taskRepo.save(new Task(
                null,
                "Zimmer aufräumen!",
                "Dein Zimmer …",
                maya,               // kamel
                maya,               // kamelTreiber
                TaskStatus.MACH_ICH_NICHT,
                new java.sql.Timestamp(System.currentTimeMillis()),
                TaskPrio.HOCH
        ));

        em.flush();
        em.clear();

    }

    /** 1) Reines Lazy: Assoziation NICHT geladen -> Zugriff außerhalb Persistence Context wirft LazyInitializationException */
    @Test
    void lazyAssociation_throwsOutsideTx() {
        var page = taskRepo.findAll(PageRequest.of(0, 1)); // KEIN fetch
        var detachedTask = page.getContent().get(0);
        em.clear(); // sicherstellen, dass nichts mehr im PC ist

        assertThatThrownBy(() -> detachedTask.getKamel().getName())
                .isInstanceOf(LazyInitializationException.class);
    }

    /** 2) EntityGraph: Assoziation vorab geladen -> Zugriff außerhalb Tx funktioniert */
    @Test
    void entityGraph_fetchesUser_okOutsideTx() {
        var page = taskRepo.findByKamel_Id(mayaId, PageRequest.of(0, 5));
        var detachedTask = page.getContent().get(0);
        em.clear();

        // sollte jetzt OHNE Lazy-Exception gehen
        assertThat(detachedTask.getKamel().getName()).isEqualTo("Maya");
    }

    /** 3) JOIN FETCH: gleiches Ergebnis wie EntityGraph */
    @Test
    void joinFetch_fetchesUser_okOutsideTx() {
        var page = taskRepo.pageByAssigneeWithUser(mayaId, PageRequest.of(0, 5));
        var detachedTask = page.getContent().get(0);
        em.clear();

        assertThat(detachedTask.getKamel().getName()).isEqualTo("Maya");
    }
}