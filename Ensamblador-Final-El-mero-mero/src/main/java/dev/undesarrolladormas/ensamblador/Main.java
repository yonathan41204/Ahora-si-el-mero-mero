package dev.undesarrolladormas.ensamblador;

// José Eduardo Mercado Hernández 
// Uriel Hernández Escobar
// Hugo Romero García
// Edson Elías Reza Garduño
import java.awt.EventQueue;
import java.awt.Image;
import java.awt.Toolkit;

import dev.undesarrolladormas.ensamblador.gui.MainWindow;
import dev.undesarrolladormas.ensamblador.persistence.Settings;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Settings.getPreferences();
            Image icon = Toolkit.getDefaultToolkit().getImage(Main.class.getResource("/icon.png"));
            MainWindow mainWindow = new MainWindow();
            mainWindow.setIconImage(icon);
            mainWindow.setVisible(true);
      
        });
    }
}
