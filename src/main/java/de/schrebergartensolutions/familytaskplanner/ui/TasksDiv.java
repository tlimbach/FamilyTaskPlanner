package de.schrebergartensolutions.familytaskplanner.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
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
@CssImport("./styles/task-styles.css")
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
        lanes.setPadding(true);
        lanes.setSpacing(true);
        lanes.setWidthFull();
        lanes.getStyle().set("overflow-x", "auto");        // horizontal scrollbar
        lanes.getStyle().set("align-items", "start");

        // Für jeden Benutzer eine Lane (= Grid<Task>) bauen
        benutzerService.findAll(Sort.by("name").ascending()).forEach(ben -> {
            Grid lane = buildUserLane(ben);
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

        btnNew.addClickListener(c -> openNewTaskDialog(null));
    }

    private void openNewTaskDialog(Task existingTask) {

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

            if (existingTask != null) task.setId(existingTask.getId());

            task.setKamelTreiber(cbxBearbeiter.getValue());
            task.setKamel(cbxBearbeiter.getValue());
            task.setStatus(cbxStatus.getValue());
            task.setPrio(cbxPrio.getValue());
            task.setTimestamp(Timestamp.valueOf(LocalDateTime.now()));
            task.setTitel(tfTitel.getValue());
            task.setBeschreibung(tfBeschreibung.getValue());
            try {
                taskService.save(task);
            } catch (Exception ex) {
                Notification.show("Fehler beim Speichern. Stelle sicher, das der Titel nicht schon verwendet wird.");
                return;
            }

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
        cbxBearbeiter.setItemLabelGenerator(b -> b.getName());

        if (existingTask != null) {
            cbxPrio.setValue(existingTask.getPrio());
            cbxStatus.setValue(existingTask.getStatus());
            tfTitel.setValue(existingTask.getTitel());
            tfBeschreibung.setValue(existingTask.getBeschreibung());
            Benutzer kamel = benutzers.stream().filter(k -> k.getId().equals(existingTask.getKamel().getId())).findFirst().get();
            cbxBearbeiter.setValue(kamel);
            cbxBearbeiter.setReadOnly(true);
        }

        // Buttons unten rechts
        HorizontalLayout buttons = new HorizontalLayout(ok, cancel);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout content = new VerticalLayout(formLayout, buttons);
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidth("480px");

        if (existingTask == null) {
            dlg.setHeaderTitle("Task anlegen");
        } else {
            dlg.setHeaderTitle("Task bearbeiten");
        }
        // Header zentrieren (einfach & robust)
        dlg.getHeader().getElement().getStyle().set("width", "100%").set("text-align", "center");

        dlg.add(content);
        dlg.open();

    }

    private Grid<Task> buildUserLane(Benutzer benutzer) {
        Grid<Task> grid = new Grid<>(Task.class, false);
        grid.setAllRowsVisible(true);             // Lanes wirken „kartenartig“
//        grid.setWidth("22rem");                   // schmale Spalte
//        grid.getStyle().set("background", "transparent");

        Span header = new Span(benutzer.getName());
        header.getStyle().set("font-weight", "bold").set("font-size", "1.1rem").set("color", "#1976d2");

        grid.addColumn(new ComponentRenderer<>(task -> {
            VerticalLayout cell = new VerticalLayout();
            cell.addClassName("task-hover");
            cell.setPadding(true);
            cell.setSpacing(true);
            cell.getStyle().set("white-space", "normal");

            // Titel fett
            Span title = new Span(task.getTitel() == null ? "(ohne Titel)" : task.getTitel());
            title.getStyle().set("font-weight", "600");

            // Beschreibung
            Paragraph desc = new Paragraph(task.getBeschreibung() == null ? "" : task.getBeschreibung());
            desc.getStyle().set("margin", "0");

            // Untere Zeile: Status + Prio
            ComboBox<TaskStatus> cbStatus = new ComboBox<>();
            cbStatus.setItems(TaskStatus.values());
            cbStatus.setValue(task.getStatus());
            cbStatus.setWidth("12rem");
            cbStatus.addValueChangeListener(ev -> {
                if (ev.isFromClient()) {
                    task.setStatus(ev.getValue());
                    taskService.save(task);
//                    grid.getDataProvider().refreshItem(task); // gezieltes Refresh nur für diese Zeile
                    grid.getDataProvider().refreshAll();
                }
            });

            ComboBox<TaskPrio> cbPrio = new ComboBox<>();
            cbPrio.setItems(TaskPrio.values());
            cbPrio.setValue(task.getPrio());
            cbPrio.setWidth("8rem");
            cbPrio.addValueChangeListener(ev -> {
                if (ev.isFromClient()) {
                    task.setPrio(ev.getValue());
                    taskService.save(task);
//                    grid.getDataProvider().refreshItem(task);
                    grid.getDataProvider().refreshAll();
                }
            });

            HorizontalLayout controls = new HorizontalLayout(cbPrio, cbStatus);
            controls.setPadding(false);
            controls.setSpacing(true);
            controls.setWidthFull();

            cell.add(title, desc, controls);
            return cell;
        })).setHeader(header).setAutoWidth(true).setFlexGrow(1);

        grid.addItemDoubleClickListener(l -> openNewTaskDialog(l.getItem()));

        // Lazy DataProvider: nur Tasks dieses Benutzers, sortiert nach Prio (hoch→niedrig) und dann Titel
        CallbackDataProvider<Task, Void> provider = DataProvider.fromCallbacks(q -> {
            int page = q.getOffset() / q.getLimit();
            int size = q.getLimit();
            // Prio-Sortierung: HOCH > MITTEL > NIEDRIG, danach Status, danach Titel
            var sort = Sort.by(Sort.Order.desc("prio"), Sort.Order.desc("status"), Sort.Order.asc("titel"));
            return taskService.pageByAssignee(benutzer.getId(), PageRequest.of(page, size, sort)).stream();
        }, q -> (int) taskService.countByAssignee(benutzer.getId()));

        providerMap.put(benutzer, provider);

        grid.setItems(provider);
        grid.setHeight("70vh");

//        VerticalLayout lane = new VerticalLayout(header, grid);
//        lane.setPadding(true);
//        lane.setSpacing(false);
//        lane.getStyle().set("border", "1px solid var(--lumo-contrast-10pct)").set("border-radius", "var(--lumo-border-radius-m)").set("background", "var(--lumo-base-color)");
        return grid;
    }


}