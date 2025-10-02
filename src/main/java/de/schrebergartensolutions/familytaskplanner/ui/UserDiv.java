package de.schrebergartensolutions.familytaskplanner.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import de.schrebergartensolutions.familytaskplanner.service.BenutzerService;
import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.flow.spring.annotation.UIScope;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UIScope
@Component
public class UserDiv extends Div {

    @Autowired
    private BenutzerService benutzerService;

    private final Grid<Benutzer> grid = new Grid<>(Benutzer.class, false);

    private final CallbackDataProvider<Benutzer, Void> dataProvider =
            DataProvider.fromCallbacks(
                    // Fetch
                    q -> {
                        int page = q.getOffset() / q.getLimit();
                        int size = q.getLimit();
                        return benutzerService
                                .page(PageRequest.of(page, size, Sort.by("name").ascending()))
                                .getContent()
                                .stream();
                    },
                    // Count
                    q -> (int) benutzerService.count()
            );

    private final Button btnEdit = new Button("Bearbeiten");
    private final Button btnDelete = new Button("Löschen");

    public UserDiv() {

        setSizeFull();
//        users.add(new Benutzer("papa", "#1976d2"));

        grid.addColumn(Benutzer::getName).setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(user -> {
            Div swatch = new Div();
            String c = user.getFarbe() == null || user.getFarbe().isBlank() ? "#ffffff" : user.getFarbe();
            swatch.getStyle()
                  .set("width", "2.5rem")
                  .set("height", "1.5rem")
                  .set("border", "1px solid #ccc")
                  .set("border-radius", "4px")
                  .set("background", c);
            swatch.getElement().setProperty("title", c);
            return swatch;
        }))
        .setHeader("Farbe")
        .setAutoWidth(true)
        .setTextAlign(ColumnTextAlign.CENTER)
        .setFlexGrow(0);

        grid.setDataProvider(dataProvider);
        grid.setSelectionMode(Grid.SelectionMode.SINGLE);
        grid.setSizeFull();

        btnEdit.setEnabled(false);
        btnDelete.setEnabled(false);

        Button btnNew = new Button("Neu");
        btnNew.addClickListener(e -> openUserDialog(null));
        btnEdit.addClickListener(e -> grid.getSelectedItems().stream().findFirst().ifPresent(this::openUserDialog));
        btnDelete.addClickListener(e -> grid.getSelectedItems().stream().findFirst().ifPresent(this::deleteUser));

        grid.addSelectionListener(e -> {
            boolean hasSel = e.getFirstSelectedItem().isPresent();
            btnEdit.setEnabled(hasSel);
            btnDelete.setEnabled(hasSel);
        });


        grid.addItemDoubleClickListener(e -> openUserDialog(e.getItem()));

        VerticalLayout right = new VerticalLayout(new H3("Aktionen"), btnNew, btnEdit, btnDelete);
        right.setPadding(true);
        right.setSpacing(true);
        right.setWidth("280px");
        right.setAlignItems(FlexComponent.Alignment.STRETCH);

        HorizontalLayout root = new HorizontalLayout(grid, right);
        root.setSizeFull();
        root.setFlexGrow(1, grid);
        add(root);
    }

    private void openUserDialog(Benutzer existing) {
        Dialog dlg = new Dialog();
        dlg.setModal(true);
        dlg.setDraggable(true);
        dlg.setResizable(false);

        TextField tfName = new TextField();
        tfName.setPlaceholder("Benutzername");
        tfName.setWidthFull();
        Input colorInput = new Input();
        colorInput.setType("color");
        colorInput.getStyle()
            .set("height", "var(--lumo-size-m)")
            .set("width",  "100%")
            .set("padding", "0")
            .set("border", "1px solid var(--lumo-contrast-20pct)")
            .set("border-radius", "var(--lumo-border-radius-m)");

        if (existing != null) {
            tfName.setValue(Objects.toString(existing.getName(), ""));
            String initial = existing.getFarbe() != null && !existing.getFarbe().isBlank() ? existing.getFarbe() : "#1976d2";
            colorInput.setValue(initial);
        } else {
            colorInput.setValue("#1976d2");
        }

        Button ok = new Button("OK", e -> {
            String name = tfName.getValue() == null ? "" : tfName.getValue().trim();
            String color = colorInput.getValue();
            if (name.isBlank() || color.isBlank()) {
                Notification.show("Bitte Name und Farbe angeben.");
                return;
            }

            if (!color.startsWith("#")) {
                color = "#" + color;
            }
            if (existing == null) {
                benutzerService.save(new Benutzer(name, color));
            } else {
                existing.setName(name);
                existing.setFarbe(color);
                benutzerService.save(existing);
            }
            dataProvider.refreshAll();

            dlg.close();
        });
        Button cancel = new Button("Abbrechen", e -> dlg.close());

        FormLayout formLayout = new FormLayout();
        formLayout.setWidthFull();
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1)); // 2 Spalten

        Span lblName = new Span("Benutzername");
        Span lblColor = new Span("Farbe");

        tfName.setWidthFull();
        formLayout.addFormItem(tfName, lblName);
        formLayout.addFormItem(colorInput, lblColor);

        // Buttons unten rechts
        HorizontalLayout buttons = new HorizontalLayout(ok, cancel);
        buttons.setWidthFull();
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);

        VerticalLayout content = new VerticalLayout(formLayout, buttons);
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidth("480px");

        if (existing == null) {
            dlg.setHeaderTitle("Benutzer anlegen");
        } else {
            dlg.setHeaderTitle("Benutzer bearbeiten");
        }
        // Header zentrieren (einfach & robust)
        dlg.getHeader().getElement().getStyle().set("width", "100%").set("text-align", "center");

        dlg.add(content);
        dlg.open();
    }

    private void deleteUser(Benutzer benutzer) {
        // Kleiner Schutz: 'papa' nicht löschen (typische MVP-Regel)
        if ("papa".equalsIgnoreCase(benutzer.getName())) {
            Notification.show("Benutzer 'papa' kann nicht gelöscht werden.");
            return;
        }
        if (benutzer.getId() != null) {
            benutzerService.delete(benutzer.getId());
            dataProvider.refreshAll();
        }

    }





}