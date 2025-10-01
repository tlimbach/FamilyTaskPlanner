package de.schrebergartensolutions.familytaskplanner.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import de.schrebergartensolutions.familytaskplanner.entities.Benutzer;
import org.vaadin.addons.tatu.ColorPicker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserDiv extends Div {

    private final Grid<Benutzer> grid = new Grid<>(Benutzer.class, false);
    private final List<Benutzer> users = new ArrayList<>();
    private final ListDataProvider<Benutzer> dataProvider = new ListDataProvider<>(users);

    private final Button btnNew = new Button("Neu");
    private final Button btnEdit = new Button("Bearbeiten");
    private final Button btnDelete = new Button("Löschen");

    public UserDiv() {
        setSizeFull();
        users.add(new Benutzer("papa", "#1976d2"));

        grid.addColumn(Benutzer::getName).setHeader("Name").setAutoWidth(true).setFlexGrow(1);
        grid.addColumn(new ComponentRenderer<>(user -> {
            Div swatch = new Div();
            String c = user.getFarbe() == null || user.getFarbe().isBlank() ? "#ffffff" : user.getFarbe();
            swatch.getStyle()
                  .set("width", "1.5rem")
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

        btnNew.addClickListener(e -> openUserDialog(null));
        btnEdit.addClickListener(e -> grid.getSelectedItems().stream().findFirst().ifPresent(this::openUserDialog));
        btnDelete.addClickListener(e -> grid.getSelectedItems().stream().findFirst().ifPresent(this::deleteUser));

        grid.addSelectionListener(e -> {
            boolean hasSel = e.getFirstSelectedItem().isPresent();
            btnEdit.setEnabled(hasSel);
            btnDelete.setEnabled(hasSel);
        });

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

        TextField tfName = new TextField("Benutzername");
        tfName.setWidthFull();
        ColorPicker colorPicker = new ColorPicker();
        colorPicker.setInputMode(ColorPicker.InputMode.NOCSSINPUT);
        colorPicker.setWidthFull();

        if (existing != null) {
            tfName.setValue(Objects.toString(existing.getName(), ""));
            colorPicker.setValue(Objects.toString(existing.getFarbe(), "#1976d2"));
        }

        Button ok = new Button("OK", e -> {
            String name = tfName.getValue() == null ? "" : tfName.getValue().trim();
            String color = Objects.toString(colorPicker.getValue(), "").trim();
            if (name.isBlank() || color.isBlank()) {
                Notification.show("Bitte Name und Farbe angeben.");
                return;
            }
            // einfache Normalisierung: sicherstellen, dass mit '#'
            if (!color.startsWith("#")) {
                color = "#" + color;
            }
            if (existing == null) {
                // neu anlegen
                users.add(new Benutzer(name, color));
                dataProvider.refreshAll();
            } else {
                existing.setName(name);
                existing.setFarbe(color);
                dataProvider.refreshItem(existing);
//                grid.getDataProvider().refreshItem(existing);
//                dataProvider.refreshAll();
            }
            dlg.close();
        });
        Button cancel = new Button("Abbrechen", e -> dlg.close());

        HorizontalLayout buttons = new HorizontalLayout(ok, cancel);
        buttons.setJustifyContentMode(FlexComponent.JustifyContentMode.END);
        buttons.setWidthFull();

        VerticalLayout content = new VerticalLayout(tfName, colorPicker, buttons);
        content.setPadding(true);
        content.setSpacing(true);
        content.setWidth("480px");

        if (existing == null) {
            dlg.setHeaderTitle("Benutzer anlegen");
        } else {
            dlg.setHeaderTitle("Benutzer bearbeiten");
        }

        dlg.add(content);
        dlg.open();
    }

    private void deleteUser(Benutzer benutzer) {
        // Kleiner Schutz: 'papa' nicht löschen (typische MVP-Regel)
        if ("papa".equalsIgnoreCase(benutzer.getName())) {
            Notification.show("Benutzer 'papa' kann nicht gelöscht werden.");
            return;
        }
        users.remove(benutzer);
        dataProvider.refreshAll();
    }


}
