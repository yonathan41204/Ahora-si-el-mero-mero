package dev.undesarrolladormas.ensamblador.persistence;

import java.lang.reflect.InvocationTargetException;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;
import javax.swing.LookAndFeel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Settings {
    private static Preferences prefs;

    private static final String THEME = "theme";

    public static void getPreferences() {
        prefs = Preferences.userNodeForPackage(Settings.class);
        String theme = prefs.get(THEME, Theme.LIGHT.getLongName());
        setTheme(theme);
    }

    public static void setTheme(String theme) {
        try {
            Class<?> c = Class.forName(theme);
            UIManager.setLookAndFeel((LookAndFeel) c.getDeclaredConstructor().newInstance());
        } catch (ClassNotFoundException | IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException | UnsupportedLookAndFeelException ex) {
            JOptionPane.showMessageDialog(null, "Error setting theme", "Error", JOptionPane.ERROR_MESSAGE);
        }
        prefs.put(THEME, theme);
    }

    public static String getTheme() {
        return prefs.get(THEME, Theme.LIGHT.getLongName());
    }
}

