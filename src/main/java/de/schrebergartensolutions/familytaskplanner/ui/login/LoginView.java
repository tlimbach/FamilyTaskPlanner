package de.schrebergartensolutions.familytaskplanner.ui.login;

import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import de.schrebergartensolutions.familytaskplanner.SessionUtils;

@PageTitle("Login | Familientaskplaner")
@Route("login")
public class LoginView extends VerticalLayout {

    private final LoginForm login = new LoginForm();

    public LoginView() {
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setForgotPasswordButtonVisible(false);
        login.addLoginListener(e -> {
            String username = e.getUsername();
            SessionUtils.set(SessionUtils.Type.user, username);
            UI.getCurrent().navigate("planner");
        });

        add(new H1("Familientaskplaner"), login);
    }
}