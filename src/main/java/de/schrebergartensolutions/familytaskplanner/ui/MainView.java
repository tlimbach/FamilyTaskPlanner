package de.schrebergartensolutions.familytaskplanner.ui;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.Tabs;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.schrebergartensolutions.familytaskplanner.SessionUtils;

@PageTitle("Familientaskplaner")
@Route("planner")
public class MainView extends VerticalLayout {

    public MainView(UserDiv userDiv) {
        setSizeFull();
        setPadding(false);
        setSpacing(false);

        H1 title = new H1("Familientaskplaner");

        String username = SessionUtils.get(SessionUtils.Type.user, String.class);
        if (username == null || username.isBlank()) {
            username = "Gast";
        }
        Span userDisplay = new Span("Benutzer: " + username);

        Button btnLogout = new Button("Abmelden");
        btnLogout.addClickListener(c->{
            SessionUtils.logout();
            UI.getCurrent().navigate("login");
        });


        // Rechte Gruppe: Benutzeranzeige + Logout nebeneinander
        HorizontalLayout rightGroup = new HorizontalLayout(userDisplay, btnLogout);
        rightGroup.setSpacing(true);
        rightGroup.setAlignItems(FlexComponent.Alignment.CENTER);

        HorizontalLayout header = new HorizontalLayout(title, rightGroup);
        header.setWidthFull();
        header.setJustifyContentMode(FlexComponent.JustifyContentMode.BETWEEN);
        header.setAlignItems(FlexComponent.Alignment.CENTER);
        header.getStyle().set("padding", "0.5em 5em");

        Div line = new Div();
        line.getStyle().set("border-bottom", "1px solid #ccc");
        line.setWidthFull();


        Tab tasksTab = new Tab("Tasks");
        Tab usersTab = new Tab("Benutzer");
        Tabs tabs = new Tabs(tasksTab, usersTab);
        tabs.setWidthFull();


        Div tasksContent = new Div();
        tasksContent.setText("→ Hier kommt später die Task-Übersicht hin");
        tasksContent.setSizeFull();

        Div content = new Div(tasksContent);
        content.setSizeFull();

        tabs.addSelectedChangeListener(event -> {
            content.removeAll();
            if (event.getSelectedTab() == tasksTab) {
                content.add(tasksContent);
            } else if (event.getSelectedTab() == usersTab) {
                content.add(userDiv);
            }
        });


        add(header, line, tabs, content);
        setFlexGrow(1, content);
    }
}