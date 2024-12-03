package dev.undesarrolladormas.ensamblador.funcs;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class PaginatedTextArea {
    private final JTextArea textArea;
    private final JPanel panel;
    private final JLabel pageLabel;
    private final JButton prevButton;
    private final JButton nextButton;

    private List<String> pages;
    private int currentPage;
    private final int linesPerPage;

    public PaginatedTextArea(int linesPerPage) {
        this.linesPerPage = linesPerPage;
        this.pages = new ArrayList<>();
        this.currentPage = 0;

        textArea = new JTextArea();
        textArea.setEditable(false);

        prevButton = new JButton("Anterior");
        nextButton = new JButton("Siguiente");
        pageLabel = new JLabel("Página 1/1", SwingConstants.CENTER);

        prevButton.addActionListener(e -> showPage(currentPage - 1));
        nextButton.addActionListener(e -> showPage(currentPage + 1));

        JPanel navigationPanel = new JPanel(new BorderLayout());
        navigationPanel.add(prevButton, BorderLayout.WEST);
        navigationPanel.add(pageLabel, BorderLayout.CENTER);
        navigationPanel.add(nextButton, BorderLayout.EAST);

        panel = new JPanel(new BorderLayout());
        panel.add(new JScrollPane(textArea), BorderLayout.CENTER);
        panel.add(navigationPanel, BorderLayout.SOUTH);

        updatePages("");
    }

    public JPanel getPanel() {
        return panel;
    }

    public void setText(String text) {
        updatePages(text);
    }

    private void updatePages(String text) {
        pages = splitIntoPages(text);
        currentPage = 0;
        showPage(0);
    }

    private List<String> splitIntoPages(String text) {
        List<String> result = new ArrayList<>();
        String[] lines = text.split("\n");

        StringBuilder page = new StringBuilder();
        int lineCount = 0;
        for (String line : lines) {
            if (lineCount == linesPerPage) {
                result.add(page.toString());
                page.setLength(0); // Reset StringBuilder
                lineCount = 0;
            }
            page.append(line).append("\n");
            lineCount++;
        }

        // Add the last page if it has content
        if (page.length() > 0) {
            result.add(page.toString());
        }

        return result;
    }

    private void showPage(int page) {
        if (page < 0 || page >= pages.size())
            return;

        currentPage = page;
        textArea.setText(pages.get(page));

        pageLabel.setText(String.format("Página %d/%d", currentPage + 1, pages.size()));
        prevButton.setEnabled(currentPage > 0);
        nextButton.setEnabled(currentPage < pages.size() - 1);
    }
}
