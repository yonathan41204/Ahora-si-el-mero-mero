package dev.undesarrolladormas.ensamblador.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import dev.undesarrolladormas.ensamblador.funcs.IdentificadorInstrucciones;
import dev.undesarrolladormas.utils.Files;

public class MainWindow extends JFrame {

    private JTextPane textArea;
    private PaginatedTable symbolTablePagination;

    public MainWindow() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Ensamblador");
        this.setSize(1400, 820);
        this.setMinimumSize(new Dimension(800, 600));
        this.setIconImage(new ImageIcon("Ensamblador-Final-El-mero-mero\\src\\main\\resources\\175181da-5df4-4711-a6e5-b389e6ea0082.jpg").getImage());
        this.init();
    }

    private void init() {
        this.setLayout(new GridBagLayout());
        JMenuBar bar = new JMenuBar();
        this.setJMenuBar(bar);

        JMenu menu = new JMenu("Archivo");
        bar.add(menu);

        // * READ FILE
        JMenuItem openItem = new JMenuItem("Abrir archivo");
        openItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openItem.addActionListener(_ -> {
            textArea.setText(Files.getFile(this));
        });
        menu.add(openItem);

        // * SAVE FILE
        JMenuItem saveItem = new JMenuItem("Guardar");
        saveItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveItem.addActionListener(_ -> {
            Files.saveFile(this, textArea.getText());
        });
        menu.add(saveItem);

        JMenu exec = new JMenu("Opciones");
        bar.add(exec);

        // Opción para actualizar direcciones
        JMenuItem actualizarDireccionesItem = new JMenuItem("Actualizar Direcciones");
        actualizarDireccionesItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));
        actualizarDireccionesItem.addActionListener(_ -> {
            try {
                String assemblyCode = textArea.getText();
                if (assemblyCode.trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Por favor, ingrese el código para actualizar direcciones.");
                    return;
                }

                // Obtener el modelo de la tabla de símbolos
                DefaultTableModel symbolTableModel = (DefaultTableModel) symbolTablePagination.table.getModel();

                // Lógica para actualizar las direcciones usando IdentificadorInstrucciones
                IdentificadorInstrucciones identificador = new IdentificadorInstrucciones();
                identificador.actualizarDirecciones(assemblyCode, symbolTableModel);

                JOptionPane.showMessageDialog(this, "Direcciones actualizadas correctamente.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al actualizar direcciones: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        exec.add(actualizarDireccionesItem);

        // --------------------------------
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 2;
        gbc.weightx = 3;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;

        textArea = new JTextPane();
        textArea.setFont(new Font("Cascadia Code NF", Font.BOLD, 14));
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        this.add(scrollPane, gbc);

        // --------------------------------
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        symbolTablePagination = new PaginatedTable(new String[]{"Símbolo", "Tipo", "Valor", "Tamaño", "Dirección"}, 10);
        this.add(symbolTablePagination.getPanel(), gbc);
    }

    /**
     * Clase interna para manejar la paginación de tablas.
     */
    private static class PaginatedTable {
        private final JTable table;
        private final DefaultTableModel model;
        private final JPanel panel;
        private final JLabel pageLabel;
        private final JButton prevButton;
        private final JButton nextButton;

        private String[][] data;
        private int currentPage;
        private final int rowsPerPage;

        public PaginatedTable(String[] columnNames, int rowsPerPage) {
            this.rowsPerPage = rowsPerPage;
            this.data = new String[0][columnNames.length];
            this.currentPage = 0;

            model = new DefaultTableModel(null, columnNames);
            table = new JTable(model);
            JScrollPane scrollPane = new JScrollPane(table);

            prevButton = new JButton("Anterior");
            nextButton = new JButton("Siguiente");
            pageLabel = new JLabel("Página 1/1", SwingConstants.CENTER);

            prevButton.addActionListener(e -> updatePage(currentPage - 1));
            nextButton.addActionListener(e -> updatePage(currentPage + 1));

            JPanel navigationPanel = new JPanel(new BorderLayout());
            navigationPanel.add(prevButton, BorderLayout.WEST);
            navigationPanel.add(pageLabel, BorderLayout.CENTER);
            navigationPanel.add(nextButton, BorderLayout.EAST);

            panel = new JPanel(new BorderLayout());
            panel.add(scrollPane, BorderLayout.CENTER);
            panel.add(navigationPanel, BorderLayout.SOUTH);

            updateTable(data);
        }

        public JPanel getPanel() {
            return panel;
        }

        public void updateTable(String[][] newData) {
            data = newData;
            currentPage = 0;
            updatePage(0);
        }

        private void updatePage(int page) {
            int totalRows = data.length;
            int totalPages = (int) Math.ceil((double) totalRows / rowsPerPage);
            if (page < 0 || page >= totalPages)
                return;

            currentPage = page;

            model.setRowCount(0);
            int start = page * rowsPerPage;
            int end = Math.min(start + rowsPerPage, totalRows);
            for (int i = start; i < end; i++) {
                model.addRow(data[i]);
            }

            pageLabel.setText(String.format("Página %d/%d", currentPage + 1, totalPages));
            prevButton.setEnabled(currentPage > 0);
            nextButton.setEnabled(currentPage < totalPages - 1);
        }
    }
}
