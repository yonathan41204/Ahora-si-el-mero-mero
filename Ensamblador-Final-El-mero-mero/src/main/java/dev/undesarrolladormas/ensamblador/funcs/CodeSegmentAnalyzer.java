package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import dev.undesarrolladormas.ensamblador.funcs.DataSegmentAnalyzer.Symbol;

public class CodeSegmentAnalyzer {

    private static final Pattern LABEL_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*:$", Pattern.CASE_INSENSITIVE);
    private static final List<String> RESERVED_WORDS = List.of(
            "add", "mov", "movzx", "or", "pop", "popa", "shr", "shl", "sub", "jmp", "jz", "main", "proc", "nop", "ends",
            ".data",
            ".code");

    // Patrones de las instrucciones seleccionadas
    private final Pattern CLD_PATTERN;
    private final Pattern CLI_PATTERN;
    private final Pattern NOP_PATTERN;
    private final Pattern POPA_PATTERN;
    private final Pattern AAD_PATTERN; // Patrón para AAD
    private final Pattern AAM_PATTERN; // Patrón para AAM
    private final Pattern MUL_PATTERN; // Patrón para MUL
    private final Pattern INC_PATTERN; // Patrón para INC

    private Set<String> declaredLabels; // Para almacenar las etiquetas declaradas
    private Set<String> declaredVariables; // Para almacenar las variables declaradas en .data

    public CodeSegmentAnalyzer(List<Symbol> vars) {
        // Patrones de las instrucciones válidas
        CLD_PATTERN = Pattern.compile("^CLD$", Pattern.CASE_INSENSITIVE); // Patrón para CLD
        CLI_PATTERN = Pattern.compile("^CLI$", Pattern.CASE_INSENSITIVE); // Patrón para CLI
        NOP_PATTERN = Pattern.compile("^NOP$", Pattern.CASE_INSENSITIVE); // Patrón para NOP
        POPA_PATTERN = Pattern.compile("^POPA$", Pattern.CASE_INSENSITIVE); // Patrón para POPA
        AAD_PATTERN = Pattern.compile("^AAD$", Pattern.CASE_INSENSITIVE); // Patrón para AAD
        AAM_PATTERN = Pattern.compile("^AAM$", Pattern.CASE_INSENSITIVE); // Patrón para AAM
        MUL_PATTERN = Pattern.compile("^MUL\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+)$", Pattern.CASE_INSENSITIVE);
        INC_PATTERN = Pattern.compile("^INC\\s+([a-zA-Z_][a-zA-Z0-9_]*|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+|\\[[^\\]]+\\])$", Pattern.CASE_INSENSITIVE); // Patrón para INC

        declaredLabels = new HashSet<>();
        declaredVariables = new HashSet<>();

        // Si `vars` contiene las variables declaradas, las añadimos al conjunto
        if (vars != null) {
            for (Symbol var : vars) {
                declaredVariables.add(var.getName().toLowerCase());
            }
        }
    }

    public List<String[]> analyze(String assemblyCode) {
        List<String[]> analysisResults = new ArrayList<>();
        String[] lines = assemblyCode.split("\\n");
        boolean inCodeSegment = false;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                continue;
            }

            if (line.startsWith(";")) {
                continue; // Ignorar comentarios
            }

            if (line.contains(";")) {
                line = line.split(";", 2)[0].trim(); // Eliminar comentarios en la misma línea
            }

            if (line.equalsIgnoreCase(".code segment") || line.equalsIgnoreCase(".code")) {
                inCodeSegment = true;
                analysisResults.add(new String[] { line, "correcta" });
                continue;
            } else if (line.equalsIgnoreCase("ends")) {
                if (inCodeSegment) {
                    inCodeSegment = false;
                    analysisResults.add(new String[] { line, "correcta" });
                }
                continue;
            } else if (line.equalsIgnoreCase(".data segment") || line.equalsIgnoreCase(".data")) {
                inCodeSegment = false;
                continue;
            }

            if (inCodeSegment) {
                if (line.endsWith(":")) {
                    if (LABEL_PATTERN.matcher(line).matches()) {
                        String label = line.substring(0, line.length() - 1).toLowerCase();
                        if (RESERVED_WORDS.contains(label)) {
                            analysisResults.add(
                                    new String[] { line, "incorrecta", "Etiqueta no puede ser palabra reservada" });
                        } else {
                            declaredLabels.add(label);
                            analysisResults.add(new String[] { line, "correcta" });
                        }
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", "Etiqueta no válida" });
                    }
                    continue;
                }

                // Validación de las instrucciones seleccionadas
                if (CLD_PATTERN.matcher(line).matches() || 
                    CLI_PATTERN.matcher(line).matches() || 
                    NOP_PATTERN.matcher(line).matches() || 
                    POPA_PATTERN.matcher(line).matches() || 
                    AAD_PATTERN.matcher(line).matches() || 
                    AAM_PATTERN.matcher(line).matches()) {
                    analysisResults.add(new String[] { line, "correcta" });
                } else if (line.toUpperCase().startsWith("MUL")) {
                    String validation = validateMUL(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
                } else if (line.toUpperCase().startsWith("INC")) {
                    String validation = validateINC(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
                } else {
                    analysisResults.add(new String[] { line, "incorrecta", "Error de sintaxis" });
                }
            }
        }

        return analysisResults;
    }

    private String validateMUL(String line) {
        if (!MUL_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica de MUL incorrecta
        }

        // Extraer el operando y verificar si es una variable declarada
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            return "Error de sintaxis"; // Debe tener un solo operando
        }

        String operand = parts[1].toLowerCase();
        if (operand.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]|[0-9]+")) {
            return "correcta"; // Operando válido (registro, inmediato o memoria)
        }

        if (declaredVariables.contains(operand)) {
            return "correcta"; // Operando es una variable declarada
        }

        return "Operando inválido"; // Operando no declarado o no permitido
    }

    private String validateINC(String line) {
        if (!INC_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica de INC incorrecta
        }

        // Extraer el operando y verificar si es una variable declarada
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            return "Error de sintaxis"; // Debe tener un solo operando
        }

        String operand = parts[1].toLowerCase();
        if (operand.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]|[0-9]+")) {
            return "correcta"; // Operando válido (registro, inmediato o memoria)
        }

        if (declaredVariables.contains(operand)) {
            return "correcta"; // Operando es una variable declarada
        }

        return "Operando inválido"; // Operando no declarado o no permitido
    }
}
