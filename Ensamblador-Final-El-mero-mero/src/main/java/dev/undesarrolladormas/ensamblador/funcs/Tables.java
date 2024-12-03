package dev.undesarrolladormas.ensamblador.funcs;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class Tables {
    public static void fillTable(JTable table, String[][] data) {
        // Obtener el modelo de la tabla
        TableModel model = table.getModel();

        // Verificar si el modelo es una instancia de DefaultTableModel
        if (!(model instanceof DefaultTableModel)) {
            // Crear un nuevo DefaultTableModel con las mismas columnas
            DefaultTableModel newModel = new DefaultTableModel(model != null ? model.getColumnCount() : 0, 0);
            for (int i = 0; model != null && i < model.getColumnCount(); i++) {
                newModel.addColumn(model.getColumnName(i));
            }
            // Asignar el nuevo modelo a la tabla
            table.setModel(newModel);
            model = newModel;
        }

        DefaultTableModel defaultModel = (DefaultTableModel) model;
        // Limpiar la tabla
        defaultModel.setRowCount(0);
        // Agregar datos
        for (String[] row : data) {
            defaultModel.addRow(row);
        }
    }
}
