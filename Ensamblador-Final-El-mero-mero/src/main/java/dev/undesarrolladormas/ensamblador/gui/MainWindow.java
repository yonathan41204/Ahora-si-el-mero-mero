package dev.undesarrolladormas.ensamblador.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

import dev.undesarrolladormas.ensamblador.funcs.CodeSegmentAnalyzer;
import dev.undesarrolladormas.ensamblador.funcs.DataSegmentAnalyzer;
import dev.undesarrolladormas.ensamblador.funcs.Identify;
import dev.undesarrolladormas.utils.Files;

public class MainWindow extends JFrame {
    public MainWindow() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setTitle("Ensamblador");
        this.setSize(1400, 820);
        this.setMinimumSize(new Dimension(800, 600));
        this.setIconImage(new ImageIcon("Ensamblador-Final-El-mero-mero\\src\\main\\resources\\175181da-5df4-4711-a6e5-b389e6ea0082.jpg").getImage());
        this.init();
    }

    private JTextPane textArea;
    private PaginatedTable tokensTablePagination;
    private PaginatedTable codificationTablePagination;
    private PaginatedTable symbolTablePagination;

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
        JMenuItem listarItem = new JMenuItem("Lista");

        listarItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
        listarItem.addActionListener(_ -> {
            // Obtener el texto del área de texto
            String assemblyCode = textArea.getText();

            if (assemblyCode.trim().isEmpty()) {
                JOptionPane.showMessageDialog(null,
                        "El área de texto está vacía. Por favor ingresa código ensamblador.");
                return;
            }

            // Tokenizar y clasificar
            String[] tokens = Identify.getTokens(assemblyCode);
            String[][] tokensData = new String[tokens.length][2];
            for (int i = 0; i < tokens.length; i++) {
                tokensData[i][0] = tokens[i];
                tokensData[i][1] = Identify.classifyToken(tokens[i]);
            }

            // Actualizar la tabla de tokens
            tokensTablePagination.updateTable(tokensData);

            // Analizar el segmento .DATA
            DataSegmentAnalyzer dataAnalyzer = new DataSegmentAnalyzer();
            List<String[]> dataResults = dataAnalyzer.analyze(assemblyCode);

            // Analizar el segmento .CODE
            CodeSegmentAnalyzer codeAnalyzer = new CodeSegmentAnalyzer(dataAnalyzer.getSymbolTable());
            List<String[]> codeResults = codeAnalyzer.analyze(assemblyCode);

            // Combinar resultados del análisis de código y datos
            List<String[]> combinedResults = new ArrayList<>();
            combinedResults.addAll(dataResults);
            combinedResults.addAll(codeResults);

            // Convertir los resultados combinados a un arreglo 2D
            String[][] resultsArray = new String[combinedResults.size()][2];
            for (int i = 0; i < combinedResults.size(); i++) {
                resultsArray[i] = combinedResults.get(i);
            }

            // Actualizar la tabla de codificación con los resultados combinados
            codificationTablePagination.updateTable(resultsArray);

            // Preparar los datos para la tabla de símbolos
            List<DataSegmentAnalyzer.Symbol> symbols = dataAnalyzer.getSymbolTable();
            String[][] symbolData = new String[symbols.size()][4];
            for (int i = 0; i < symbols.size(); i++) {
                DataSegmentAnalyzer.Symbol symbol = symbols.get(i);
                symbolData[i][0] = symbol.getName();
                symbolData[i][1] = symbol.getType();
                symbolData[i][2] = symbol.getValue();
                symbolData[i][3] = String.valueOf(symbol.getSize());
            }

            // Actualizar la tabla de símbolos
            symbolTablePagination.updateTable(symbolData);
        });
        exec.add(listarItem);

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
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        tokensTablePagination = new PaginatedTable(new String[] { "Palabra", "Tipo" }, 10);
        this.add(tokensTablePagination.getPanel(), gbc);

        // --------------------------------
        gbc.gridx = 1;
        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.gridheight = 1;
        gbc.weightx = 2;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        codificationTablePagination = new PaginatedTable(new String[] { "Línea", "Verificació","CP" }, 10);
        this.add(codificationTablePagination.getPanel(), gbc);

        // --------------------------------
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.gridheight = 1;
        gbc.weightx = 1;
        gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        symbolTablePagination = new PaginatedTable(new String[] { "Símbolo", "Tipo", "Valor", "Tamaño", "Direccion"}, 10);
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