package de.schrebergartensolutions.familytaskplanner;

import com.vaadin.flow.server.VaadinSession;

public final class SessionUtils {

  public static enum Type {user}

  private SessionUtils() {}

  public static void set(Type type, String value) {
    VaadinSession.getCurrent().setAttribute(type.name(), value);
  }

  public static void setString(String key, String value) {
    VaadinSession.getCurrent().setAttribute(key, value);
  }

  @SuppressWarnings("unchecked")
  public static <T> T get(Type key, Class<T> type) {
    Object value = VaadinSession.getCurrent().getAttribute(key.name());
    return type.isInstance(value) ? (T) value : null;
  }

  public static String getString(String key) {
    return (String) VaadinSession.getCurrent().getAttribute(key);
  }
  public static void logout() {

    VaadinSession session = VaadinSession.getCurrent();
    //TODO: hier alle Attribute löschen
    if (session != null) {
      session.close();
    }
  }

}