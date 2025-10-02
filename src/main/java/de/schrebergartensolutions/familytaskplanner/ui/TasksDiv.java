package de.schrebergartensolutions.familytaskplanner.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.spring.annotation.UIScope;
import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import de.schrebergartensolutions.familytaskplanner.entities.Task;
import de.schrebergartensolutions.familytaskplanner.entities.TaskPrio;
import de.schrebergartensolutions.familytaskplanner.entities.TaskStatus;
import de.schrebergartensolutions.familytaskplanner.service.BenutzerService;
import de.schrebergartensolutions.familytaskplanner.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
@UIScope
public class TasksDiv extends Div {

    private final BenutzerService benutzerService;
    private final TaskService taskService;
    private Map<Benutzer, DataProvider> providerMap = new HashMap<>();
    public TasksDiv(BenutzerService benutzerService, TaskService taskService) {
        this.benutzerService = benutzerService;
        this.taskService = taskService;
        buildUI();
    }

    private void buildUI() {
        setSizeFull();

        // ---------- LINKS: 80% (Lanes je Benutzer) ----------
        HorizontalLayout lanes = new HorizontalLayout();
        lanes.setPadding(false);
        lanes.setSpacing(true);
        lanes.setWidthFull();
        lanes.getStyle().set("overflow-x", "auto");        // horizontal scrollbar
        lanes.getStyle().set("align-items", "start");

        // Für jeden Benutzer eine Lane (= Grid<Task>) bauen
        benutzerService.findAll(Sort.by("name").ascending()).forEach(ben -> {
            VerticalLayout lane = buildUserLane(ben);
            lanes.add(lane);
        });

        // Wrapper, der links 80% belegt
        Div left = new Div(lanes);
        left.setWidthFull();
        left.getStyle().set("flex", "1 1 80%")   // 80% Breite
                .set("min-width", "0");   // wichtig für flex+scroll

        // ---------- RECHTS: 20% (Aktionen) ----------
        Button btnNew = new Button("Neuen Task erstellen");
        Button btnDelete = new Button("Task löschen");
        VerticalLayout actions = new VerticalLayout(new H3("Aktionen"), btnNew, btnDelete);
        actions.setWidthFull();
        actions.setSpacing(true);
        actions.setPadding(true);

        Div right = new Div(actions);
        right.getStyle().set("flex", "0 0 20%")  // fix ~20%
                .set("border-left", "1px solid var(--lumo-contrast-10pct)").set("padding-left", "var(--lumo-space-m)");

        // ---------- ROOT ----------
        HorizontalLayout root = new HorizontalLayout(left, right);
        root.setSizeFull();
        root.setSpacing(false);
        root.setPadding(false);
        root.getStyle().set("display", "flex");
        add(root);

        btnNew.addClickListener(c -> openNewTaskDialog());
    }

    private void openNewTaskDialog() {

        TextField tfTitel = new TextField();
        ComboBox<TaskPrio> cbxPrio = new ComboBox<>();
        ComboBox<TaskStatus> cbxStatus = new ComboBox<>();
        ComboBox<Benutzer> cbxBearbeiter = new ComboBox<>();
        TextField tfBeschreibung = new TextField();


        Dialog dlg = new Dialog();
        dlg.setModal(true);
        dlg.setDraggable(true);
        dlg.setResizable(false);

        Button ok = new Button("OK", e -> {

            Task task = new Task();
            task.setKamelTreiber(cbxBearbeiter.getValue());
            task.setKamel(cbxBearbeiter.getValue());
            task.setStatus(cbxStatus.getValue());
            task.setPrio(cbxPrio.getValue());
            task.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            task.setTitel(tfTitel.getValue());
            task.setBeschreibung(tfBeschreibung.getValue());

            taskService.save(task);

            providerMap.get(task.getKamel()).refreshAll();


            dlg.close();
        });
        Button cancel = new Button("Abbrechen", e -> dlg.close());

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1)); // 2 Spalten



        tfTitel.setPlaceholder("Neuer Task");


        tfBeschreibung.setPlaceholder("Neuer Task");
        tfBeschreibung.setWidthFull();


        formLayout.addFormItem(tfTitel, new Span("Titel"));
        formLayout.addFormItem(tfBeschreibung, new Span("Beschreibung"));
        formLayout.addFormItem(cbxPrio, new Span("Priorität"));
        formLayout.addFormItem(cbxStatus, new Span("Status"));
        formLayout.addFormItem(cbxBearbeiter, new Span("Bearbeiter"));

        cbxPrio.setItems(TaskPrio.values());
        cbxStatus.setItems(TaskStatus.values());
        List<Benutzer> benutzers = benutzerService.findAll(Sort.by("name").ascending());
        cbxBearbeiter.setItems(benutzers);
        cbxBearbeiter.setItemLabelGenerator(b->b.getName());

        // Buttons unten rechts
        HorizontalLayout buttons = new HorizontalLayout(ok, cancel);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout content = new VerticalLayout(formLayout, buttons);
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidth("480px");

        if (2>1) {
            dlg.setHeaderTitle("Task anlegen");
        } else {
            dlg.setHeaderTitle("Task bearbeiten");
        }
        // Header zentrieren (einfach & robust)
        dlg.getHeader().getElement().getStyle().set("width", "100%").set("text-align", "center");

        dlg.add(content);
        dlg.open();

    }

    private VerticalLayout buildUserLane(Benutzer benutzer) {
        // Überschrift = Benutzername
        H4 header = new H4(benutzer.getName());
        header.getStyle().set("margin", "0 var(--lumo-space-xs) var(--lumo-space-s) var(--lumo-space-xs)");

        Grid<Task> grid = new Grid<>(Task.class, false);
        grid.setAllRowsVisible(true);             // Lanes wirken „kartenartig“
        grid.setWidth("22rem");                   // schmale Spalte
        grid.getStyle().set("background", "transparent");

        // Spalte: Titel + Beschreibung (in einer Zelle)
        grid.addColumn(new ComponentRenderer<>(task -> {
            Div cell = new Div();
            cell.getStyle().set("white-space", "normal");
            Span title = new Span(task.getTitel() == null ? "(ohne Titel)" : task.getTitel());
            title.getStyle().set("font-weight", "600");
            Paragraph desc = new Paragraph(task.getBeschreibung() == null ? "" : task.getBeschreibung());
            desc.getStyle().set("margin", "0");
            cell.add(title, desc);
            return cell;
        })).setHeader("Task").setAutoWidth(true).setFlexGrow(1);

        // Spalte: Status (ComboBox, speichert sofort)
        grid.addColumn(new ComponentRenderer<>(task -> {
            ComboBox<TaskStatus> cb = new ComboBox<>();
            cb.setItems(TaskStatus.values());
            cb.setValue(task.getStatus());
            cb.setWidthFull();
            cb.addValueChangeListener(ev -> {
                if (ev.isFromClient()) {
                    task.setStatus(ev.getValue());
                    taskService.save(task);
                    grid.getDataProvider().refreshItem(task);
                }
            });
            return cb;
        })).setHeader("Status").setAutoWidth(true).setFlexGrow(0);

        // Optional: Priorität lesen (nur Anzeige)
        grid.addColumn(task -> task.getPrio() == null ? "" : task.getPrio().name()).setHeader("Prio").setAutoWidth(true).setFlexGrow(0);

        // Lazy DataProvider: nur Tasks dieses Benutzers, sortiert nach Prio (hoch→niedrig) und dann Titel
        CallbackDataProvider<Task, Void> provider = DataProvider.fromCallbacks(q -> {
            int page = q.getOffset() / q.getLimit();
            int size = q.getLimit();
            // Prio-Sortierung: HOCH > MITTEL > NIEDRIG, danach Titel
            var sort = Sort.by(Sort.Order.desc("prio"), Sort.Order.asc("titel"));
            return taskService.pageByAssignee(benutzer.getId(), PageRequest.of(page, size, sort)).stream();
        }, q -> (int) taskService.countByAssignee(benutzer.getId()));

        providerMap.put(benutzer, provider);

        grid.setItems(provider);
        grid.setHeight("70vh");

        VerticalLayout lane = new VerticalLayout(header, grid);
        lane.setPadding(true);
        lane.setSpacing(false);
        lane.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-m)").set("background", "var(--lumo-base-color)");
        return lane;
    }


}