package dev.undesarrolladormas.ensamblador;

// José Eduardo Mercado Hernández 
// Uriel Hernández Escobar
// Hugo Romero García
// Edson Elías Reza Garduño
import java.awt.EventQueue;


import dev.undesarrolladormas.ensamblador.gui.MainWindow;
import dev.undesarrolladormas.ensamblador.persistence.Settings;

public class Main {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            Settings.getPreferences();
            MainWindow mainWindow = new MainWindow();
         
            mainWindow.setVisible(true);
      
        });
    }
}
