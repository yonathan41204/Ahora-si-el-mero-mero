package dev.undesarrolladormas.ensamblador.funcs;

import java.util.regex.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;

public class IdentificadorInstrucciones {

    private DefaultTableModel modelo; // La tabla para mostrar resultados
    private int programCounter; // Contador del programa (inicia en 0250h)

    public void identificador(String texto, DefaultTableModel modelo) {
        programCounter = 0x0250; // Reiniciar el contador del programa al analizar una nueva página
    
        // Dividir el texto en líneas
        String[] lineas = texto.split("\\r?\\n");
    
        for (String linea : lineas) {
            linea = linea.trim();
    
            // Ignorar líneas con "Error" después de un guion
            if (linea.contains("-")) {
                if (linea.contains("Error")) {
                    continue; // Ignorar la línea si contiene un error
                } else {
                    linea = linea.split("-", 2)[0].trim(); // Eliminar texto desde el guion
                }
            }
    
            // Analizar la línea para identificar instrucciones
            analizarInstruccion(linea, modelo);
        }
    }
    
    private void analizarInstruccion(String linea, DefaultTableModel modelo) {
        Pattern patron = Pattern.compile("^(\\w+)?\\s*(db|dw|dd)\\s*(.*)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(linea);
    
        if (matcher.find()) {
            String simbolo = matcher.group(1) != null ? matcher.group(1) : "anónimo";
            String tipo = matcher.group(2).toLowerCase();
            String valor = matcher.group(3);
    
            // Calcular tamaño en bytes
            int tamano = calcularTamano(tipo, valor);
    
            // Agregar el resultado a la tabla
            modelo.addRow(new Object[]{
                simbolo,
                "variable",
                valor,
                tamano,
                String.format("%04X", programCounter) // Mostrar el contador del programa
            });
    
            // Actualizar el contador del programa
            programCounter += tamano;
        }
    }
    
    private int calcularTamano(String tipo, String valor) {
        int tamano = 0;

        // Calcular el tamaño según el tipo
        switch (tipo) {
            case "db":
                if (valor.startsWith("'") && valor.endsWith("'")) {
                    // Es una cadena: calcular su longitud
                    tamano = valor.length() - 2; // Excluir comillas simples
                } else {
                    tamano = 1; // Un byte por defecto
                }
                break;

            case "dw":
                if (valor.toLowerCase().contains("dup")) {
                    tamano = calcularDup(valor) * 2; // Cada dw ocupa 2 bytes
                } else {
                    tamano = 2; // Un valor normal dw ocupa 2 bytes
                }
                break;

            case "dd":
                if (valor.toLowerCase().contains("dup")) {
                    tamano = calcularDup(valor) * 4; // Cada dd ocupa 4 bytes
                } else {
                    tamano = 4; // Un valor normal dd ocupa 4 bytes
                }
                break;
        }
        return tamano;
    }

    public void actualizarDirecciones(DefaultTableModel modelo) {
        programCounter = 0x0250; // Reiniciar contador del programa
    
        // Iterar sobre cada fila existente en la tabla
        for (int i = 0; i < modelo.getRowCount(); i++) {
            // Calcular dirección en hexadecimal
            String direccion = String.format("%04X", programCounter);
    
            // Actualizar la columna de dirección (suponiendo que es la última columna)
            modelo.setValueAt(direccion, i, modelo.getColumnCount() - 1);
    
            // Obtener el tipo y valor de la fila actual para calcular el tamaño
            String tipo = (String) modelo.getValueAt(i, 1); // Suponiendo que la columna 1 es el tipo
            String valor = (String) modelo.getValueAt(i, 2); // Suponiendo que la columna 2 es el valor
    
            // Incrementar el contador del programa usando el tamaño calculado
            int tamano = calcularTamano(tipo, valor);
            programCounter += tamano;
        }
    }
    

    

    private int calcularDup(String valor) {
        // Patrón para extraer el número antes y dentro de DUP (e.g., 100 DUP(0))
        Pattern patron = Pattern.compile("(\\d+)\\s*dup\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(valor);

        if (matcher.find()) {
            int repeticiones = Integer.parseInt(matcher.group(1)); // Número antes de DUP
            return repeticiones;
        }
        return 0; // Si no es válido, asumimos 0
    }
}