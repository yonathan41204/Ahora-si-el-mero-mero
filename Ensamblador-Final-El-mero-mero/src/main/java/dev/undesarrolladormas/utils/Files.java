package dev.undesarrolladormas.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Files {
    // Key for storing the last directory
    private static final String LAST_DIRECTORY_KEY = "lastDirectory";
    private static final Preferences prefs = Preferences.userRoot().node(Files.class.getName());

    public static String getFile(JFrame frame) {
        // Retrieve the last directory from preferences
        String lastDirectory = prefs.get(LAST_DIRECTORY_KEY, null);

        // Configure file chooser
        JFileChooser fileChooser = new JFileChooser();
        if (lastDirectory != null) {
            fileChooser.setCurrentDirectory(new File(lastDirectory));
        }
        fileChooser.setDialogTitle("Abrir archivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ensamblador por max", "ens");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show dialog
        int result = fileChooser.showOpenDialog(frame);
        if (result != JFileChooser.APPROVE_OPTION) {
            return null;
        }

        // Get the selected file
        File file = fileChooser.getSelectedFile();
        if (file == null || !file.exists()) {
            return null;
        }

        // Save the directory of the selected file to preferences
        prefs.put(LAST_DIRECTORY_KEY, file.getParent());

        // Read file content
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new java.io.FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(frame, "Error al cargar: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
        return content.toString();
    }

    public static void saveFile(JFrame frame, String content) {
        // Retrieve the last directory from preferences
        String lastDirectory = prefs.get(LAST_DIRECTORY_KEY, null);

        // Configure file chooser
        JFileChooser fileChooser = new JFileChooser();
        if (lastDirectory != null) {
            fileChooser.setCurrentDirectory(new File(lastDirectory));
        }
        fileChooser.setDialogTitle("Guardar archivo");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Ensambaldor pro Max", "ens");
        fileChooser.setFileFilter(filter);
        fileChooser.setAcceptAllFileFilterUsed(false);

        // Show dialog
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();

            // Ensure the filename has the correct extension
            String filename = file.toString();
            if (!filename.endsWith(".ens")) {
                filename += ".ens";
            }
            file = new File(filename);

            // Save the directory to preferences
            prefs.put(LAST_DIRECTORY_KEY, file.getParent());

            // Write content to file
            try (java.io.FileWriter writer = new java.io.FileWriter(file)) {
                writer.write(content);
            } catch (IOException ex) {
                javax.swing.JOptionPane.showMessageDialog(frame, "Error al abrir el archivo: " + ex.getMessage(), "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
