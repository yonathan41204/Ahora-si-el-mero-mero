package dev.undesarrolladormas.ensamblador.funcs;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PaginatedTableModel extends AbstractTableModel {
    private final List<String[]> data;
    private final String[] columnNames;
    private int pageSize;
    private int currentPage;

    public PaginatedTableModel(List<String[]> data, String[] columnNames, int pageSize) {
        this.data = data;
        this.columnNames = columnNames;
        this.pageSize = pageSize;
        this.currentPage = 0;
    }

    @Override
    public int getRowCount() {
        return Math.min(pageSize, data.size() - currentPage * pageSize);
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        int actualRowIndex = currentPage * pageSize + rowIndex;
        return data.get(actualRowIndex)[columnIndex];
    }

    public void nextPage() {
        if ((currentPage + 1) * pageSize < data.size()) {
            currentPage++;
            fireTableDataChanged();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            fireTableDataChanged();
        }
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getTotalPages() {
        return (int) Math.ceil((double) data.size() / pageSize);
    }
}
