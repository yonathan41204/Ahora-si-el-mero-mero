package dev.undesarrolladormas.ensamblador;

import java.awt.EventQueue;


import dev.undesarrolladormas.ensamblador.gui.MainWindow;
import dev.undesarrolladormas.ensamblador.persistence.Settings;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Settings.getPreferences();
            MainWindow mainWindow = new MainWindow();
            mainWindow.setLocationRelativeTo(null);
            mainWindow.setVisible(true);
      
        });
    }
}
