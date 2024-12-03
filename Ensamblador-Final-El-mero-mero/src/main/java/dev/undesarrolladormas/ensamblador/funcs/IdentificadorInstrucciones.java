package dev.undesarrolladormas.ensamblador.funcs;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.DefaultTableModel;

public class IdentificadorInstrucciones {

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
            String tipo = matcher.group(2).toLowerCase();
            String valor = matcher.group(3);

            // Calcular tamaño en bytes
            int tamano = calcularTamano(tipo, valor);

            // Buscar fila con el símbolo en la tabla
            boolean filaActualizada = false;
            for (int i = 0; i < modelo.getRowCount(); i++) {
                String simbolo = (String) modelo.getValueAt(i, 0);
                if (simbolo != null && simbolo.equals(matcher.group(1))) {
                    modelo.setValueAt(String.format("%04X", programCounter), i, 4); // Actualizar columna 'Direccion'
                    filaActualizada = true;
                    break;
                }
            }

            // Si no se encontró una fila existente, agregar una nueva
            if (!filaActualizada) {
                modelo.addRow(new Object[]{
                    matcher.group(1) != null ? matcher.group(1) : "anónimo", // Simbolo
                    "variable", // Tipo
                    valor, // Valor
                    tamano, // Tamaño
                    String.format("%04X", programCounter) // Direccion
                });
            }

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

    private int calcularDup(String valor) {
        // Patrón para extraer el número antes y dentro de DUP (e.g., 100 DUP(0))
        Pattern patron = Pattern.compile("(\\d+)\\s*dup\\((\\d+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = patron.matcher(valor);

        if (matcher.find()) {
            int repeticiones = Integer.parseInt(matcher.group(1)); // Número antes de DUP
            return repeticiones;
        }
        return 0; // Si no es válido, asumimos 0
    }
}
