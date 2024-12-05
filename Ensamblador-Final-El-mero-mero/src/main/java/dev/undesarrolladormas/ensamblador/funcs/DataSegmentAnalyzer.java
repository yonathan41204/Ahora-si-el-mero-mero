package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DataSegmentAnalyzer {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(
            "^\\s*\\w+\\s+(DB|DW|DD|DQ|DT|DF|DP|DQWORD|REAL4|REAL8|REAL10|BYTE|SBYTE|WORD|SWORD|DWORD|SDWORD|FWORD|QWORD|TBYTE)\\s+.*",
            Pattern.CASE_INSENSITIVE);
    private static final Pattern DIRECTIVE_PATTERN = Pattern.compile(
            "^(DB|DW|DD|DQ|DT|DF|DP|DQWORD|BYTE|SBYTE|WORD|SWORD|DWORD|SDWORD|FWORD|QWORD|TBYTE)$",
            Pattern.CASE_INSENSITIVE);
    private final List<Symbol> symbolTable = new ArrayList<>();
    private int currentAddress = 0x0250; // Dirección inicial en hexadecimal

    public List<String[]> analyze(String assemblyCode) {
        List<String[]> analysisResults = new ArrayList<>();
        String[] lines = assemblyCode.split("\\n");
        boolean inDataSegment = false;
        boolean inStackSegment = false;
    
        for (String line : lines) {
            line = line.trim();
    
            // Identificar el inicio de segmentos
            if (line.equalsIgnoreCase(".data segment") || line.equalsIgnoreCase(".data")) {
                inDataSegment = true;
                inStackSegment = false;
                currentAddress = 0x0250; // Reiniciar el contador de programa para el segmento de datos
                analysisResults.add(new String[] { line, "correcta", "" });
                continue;
            } else if (line.equalsIgnoreCase(".stack segment") || line.equalsIgnoreCase(".stack")) {
                inDataSegment = false;
                inStackSegment = true;
                analysisResults.add(new String[] { line, "correcta", String.format("%04XH", currentAddress) });
                continue;
            } else if (line.equalsIgnoreCase("ends")) {
                if (inDataSegment || inStackSegment) {
                    analysisResults.add(new String[] { line, "correcta", "" });
    
                    // Imprimir el valor final del contador de programa para el segmento actual
                    System.out.println("Final del segmento: Dirección actual -> " + String.format("%04XH", currentAddress));
                }
                inDataSegment = false;
                inStackSegment = false;
                continue;
            } else if (line.equalsIgnoreCase(".code segment") || line.equalsIgnoreCase(".code")) {
                inDataSegment = false;
                inStackSegment = false;
                continue;
            }
    
            // Analizar líneas dentro de segmentos
            if (inDataSegment) {
                if (line.isEmpty() || line.startsWith(";")) {
                    continue;
                }
    
                String[] result = analyzeDataLine(line);
                analysisResults.add(result);
                if (result[1].equals("correcta")) {
                    addSymbolToTable(line, result[2]);
                }
            } else if (inStackSegment) {
                if (line.isEmpty() || line.startsWith(";")) {
                    continue;
                }

                String[] result = analyzeStackLine(line);
                analysisResults.add(result);
            }
        }
        return analysisResults;
    }
    
    private String[] analyzeDataLine(String line) {
    String[] parts = line.split("\\s+", 3); // Divide la línea en máximo 3 partes para preservar el valor
    if (parts.length >= 3 && isValidDataLine(line) && !isDirective(parts[2])) {
        String name = parts[0];
        String value = parts[2]; // Captura el valor completo (puede ser una cadena o número)

        // Validar longitud del nombre
        if (name.length() > 10) {
            return new String[] { line, "incorrecta", "Nombre mayor de 10 caracteres" };
        }

        // Validar duplicado
        for (Symbol symbol : symbolTable) {
            if (symbol.getName().equalsIgnoreCase(name)) {
                return new String[] { line, "incorrecta", "Nombre duplicado" };
            }
        }

        // Validar cadenas entre comillas simples o dobles
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            String address = String.format("%04XH", currentAddress);

            // Calcular tamaño: Cada carácter cuenta como 1 byte
            int size = value.length() - 2; // Restar las comillas de apertura y cierre
            if (size > 0) {
                currentAddress += size;
            } else {
                return new String[] { line, "incorrecta", "Cadena vacía o mal formada" };
            }

            return new String[] { line, "correcta", address };
        }

        // Validar valores binarios
        if (value.toUpperCase().endsWith("B")) {
            String binaryValue = value.substring(0, value.length() - 1); // Remover la 'B'
            if (!binaryValue.matches("^[01]+$")) {
                return new String[] { line, "incorrecta", "Valor binario no válido" };
            }
            if (parts[1].equalsIgnoreCase("DB") && binaryValue.length() != 8) {
                return new String[] { line, "incorrecta", "DB requiere 8 bits" };
            }
            if (parts[1].equalsIgnoreCase("DW") && binaryValue.length() != 16) {
                return new String[] { line, "incorrecta", "DW requiere 16 bits" };
            }
            String address = String.format("%04XH", currentAddress);
            int size = calculateSize(parts[1]);
            currentAddress += size;
            return new String[] { line, "correcta", address };
        }

        // Validar valores hexadecimales
        if (value.toUpperCase().endsWith("H")) {
            String hexValue = value.substring(0, value.length() - 1); // Remover la 'H'
            if (hexValue.matches("^[0-9A-Fa-f]+$")) {
                String address = String.format("%04XH", currentAddress);
                int size = calculateSize(parts[1]);
                currentAddress += size;
                return new String[] { line, "correcta", address };
            } else {
                return new String[] { line, "incorrecta", "Valor hexadecimal no válido" };
            }
        }
        // Validar valores decimales
        try {
            Integer.parseInt(value); // Intentar parsear como número decimal
            String address = String.format("%04XH", currentAddress);
            int size = calculateSize(parts[1]);
            currentAddress += size;
            return new String[] { line, "correcta", address };
        } catch (NumberFormatException e) {
            return new String[] { line, "incorrecta", "Valor no válido" };
        }
    } else {
        return new String[] { line, "incorrecta", "Error en la sintaxis" };
    }
}

    

    private boolean isValidDataLine(String line) {
        return VARIABLE_PATTERN.matcher(line).matches();
    }

    private boolean isDirective(String value) {
        return DIRECTIVE_PATTERN.matcher(value).matches();
    }

    // Metodo para agregar a la tabla de simbolos creo we
    private void addSymbolToTable(String line, String address) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 3) {
            String name = parts[0];

            // Verificar si el nombre ya existe en la tabla de símbolos
            for (Symbol symbol : symbolTable) {
                if (symbol.getName().equalsIgnoreCase(name)) {
                    // Si el nombre existe, no lo agrega
                    System.err.println("Error ya está definido.");
                    return;
                }
            }

            String type = "variable";
            String value = parts[2];
            String size = getSizeFromDirective(parts[1]);
            symbolTable.add(new Symbol(name, type, value, size, address));
        }
    }

    private int calculateSize(String directive) {
        return switch (directive.toUpperCase()) {
            case "DB", "BYTE", "SBYTE" -> 1;
            case "DW", "WORD", "SWORD" -> 2;
            case "DD", "DWORD", "SDWORD" -> 4;
            case "DQ", "QWORD" -> 8;
            case "DT", "TBYTE" -> 10;
            default -> 0; // Asume 0 si el tamaño no está definido
        };
    }

    private String getSizeFromDirective(String directive) {
        return switch (directive.toUpperCase()) {
            case "DB", "BYTE" -> "8 / BYTE";
            case "DW", "WORD" -> "16 / WORD";
            case "DD", "DWORD" -> "32 / DWORD";
            case "DQ", "QWORD" -> "64 bits / QWORD";
            case "DT", "TBYTE" -> "80 bits / TBYTE";
            case "SBYTE" -> "8 bits / SIGNED BYTE";
            case "SWORD" -> "16 bits / SIGNED WORD";
            case "SDWORD" -> "32 bits / SIGNED DWORD";
            default -> "desconocido";
        };
    }

    private String[] analyzeStackLine(String line) {
        // Patrón para validar `dw constante DUP(valor)`
        Pattern stackPattern = Pattern.compile("^\\s*dw\\s+\\d+\\s+dup\\(\\s*(-?\\d+)\\s*\\)\\s*$",
                Pattern.CASE_INSENSITIVE);

        if (stackPattern.matcher(line).matches()) {
            String[] parts = line.split("\\s+"); // Dividimos la línea para capturar los valores
            try {
                // Extraer el tamaño y calcular el total
                int repetitions = Integer.parseInt(parts[1]); // Número de repeticiones
                int valueSize = 2; // Cada `dw` ocupa 2 bytes por valor
                int size = repetitions * valueSize;
    
                String address = String.format("%04XH", currentAddress);
                currentAddress += size; // Actualizamos el contador de programa
    
                return new String[] { line, "correcta", address };
            } catch (NumberFormatException e) {
                return new String[] { line, "incorrecta", "Error al interpretar valores del stack" };
            }
        } else {
            return new String[] { line, "incorrecta", "Error en la sintaxis" };
        }
    }
    
    public List<Symbol> getSymbolTable() {
        return symbolTable;
    }

    public static class Symbol {
        private final String name;
        private final String type;
        private final String value;
        private final String size;
        private final String address;

        public Symbol(String name, String type, String value, String size, String address) {
            this.name = name;
            this.type = type;
            this.value = value;
            this.size = size;
            this.address = address;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public String getValue() {
            return value;
        }

        public String getSize() {
            return size;
        }

        public String getAddress() {
            return address;
        }

        
    }
}