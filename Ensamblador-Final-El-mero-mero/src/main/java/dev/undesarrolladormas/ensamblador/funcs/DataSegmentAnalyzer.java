package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class DataSegmentAnalyzer {
    private static final Pattern VARIABLE_PATTERN = Pattern.compile(
        "^\\s*\\w+\\s+(DB|DW|DD|DQ|DT|DF|DP|DQWORD|REAL4|REAL8|REAL10|BYTE|SBYTE|WORD|SWORD|DWORD|SDWORD|FWORD|QWORD|TBYTE)\\s+.*",
        Pattern.CASE_INSENSITIVE
    );
    private static final Pattern DIRECTIVE_PATTERN = Pattern.compile(
        "^(DB|DW|DD|DQ|DT|DF|DP|DQWORD|BYTE|SBYTE|WORD|SWORD|DWORD|SDWORD|FWORD|QWORD|TBYTE)$",
        Pattern.CASE_INSENSITIVE
    );
    private final List<Symbol> symbolTable = new ArrayList<>();
    private int currentAddress = 0x0250; // Dirección inicial en hexadecimal

    public List<String[]> analyze(String assemblyCode) {
        List<String[]> analysisResults = new ArrayList<>();
        String[] lines = assemblyCode.split("\\n");
        boolean inDataSegment = false;
        boolean inStackSegment = false;

        for (String line : lines) {
            line = line.trim();

            if (line.equalsIgnoreCase(".data segment") || line.equalsIgnoreCase(".data")) {
                inDataSegment = true;
                analysisResults.add(new String[] { line, "correcta", "" });
                continue;
            } else if (line.equalsIgnoreCase(".stack segment") || line.equalsIgnoreCase(".stack")) {
                inDataSegment = false;
                inStackSegment = true;
                analysisResults.add(new String[] { line, "correcta", "" });
                continue;
            } else if (line.equalsIgnoreCase("ends")) {
                if (inDataSegment || inStackSegment) {
                    analysisResults.add(new String[] { line, "correcta", "" });
                }
                inDataSegment = false;
                inStackSegment = false;
                continue;
            } else if (line.equalsIgnoreCase(".code segment") || line.equalsIgnoreCase(".code")) {
                inDataSegment = false;
                inStackSegment = false;
                continue;
            }

            if (inDataSegment) {
                if (line.isEmpty() || line.startsWith(";")) {
                    continue;
                }

                String[] result = analyzeLine(line);
                analysisResults.add(result);
                if (result[1].equals("correcta")) {
                    addSymbolToTable(line, result[2]);
                }
            } else if (inStackSegment) {
                analysisResults.add(new String[] { line, "correcta", "" });
            }
        }
        return analysisResults;
    }

    private String[] analyzeLine(String line) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 3 && isValidDataLine(line) && !isDirective(parts[2])) {
            String address = String.format("%04XH", currentAddress);
            int size = calculateSize(parts[1]);
            currentAddress += size;
            return new String[] { line, "correcta", address };
        } else {
            return new String[] { line, "incorrecta", "" };
        }
    }

    private boolean isValidDataLine(String line) {
        return VARIABLE_PATTERN.matcher(line).matches();
    }

    private boolean isDirective(String value) {
        return DIRECTIVE_PATTERN.matcher(value).matches();
    }

    private void addSymbolToTable(String line, String address) {
        String[] parts = line.split("\\s+");
        if (parts.length >= 3) {
            String name = parts[0];
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
            case "DB", "BYTE" -> "1 / BYTE";
            case "DW", "WORD" -> "1 / WORD";
            case "DD", "DWORD" -> "1 / DWORD";
            case "DQ", "QWORD" -> "64 bits / QWORD";
            case "DT", "TBYTE" -> "80 bits / TBYTE";
            case "SBYTE" -> "8 bits / SIGNED BYTE";
            case "SWORD" -> "16 bits / SIGNED WORD";
            case "SDOUBLE" -> "32 bits / SIGNED DOUBLE";
            default -> "desconocido";
        };
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

        @Override
        public String toString() {
            return "Symbol{" +
                    "name='" + name + '\'' +
                    ", type='" + type + '\'' +
                    ", value='" + value + '\'' +
                    ", size='" + size + '\'' +
                    ", address='" + address + '\'' +
                    '}';
        }
    }
}
