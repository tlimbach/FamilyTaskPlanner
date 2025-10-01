package de.schrebergartensolutions.familytaskplanner.ui;

import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

@PageTitle("Willkommen | Familientaskplaner")
@Route("planner")
public class WelcomeView extends VerticalLayout {

    public WelcomeView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        String username = (String) VaadinSession.getCurrent().getAttribute("user");
        if (username == null || username.isBlank()) {
            username = "Gast";
        }

        add(new H2("Willkommen, " + username + "!"));
    }
}