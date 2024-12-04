package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import dev.undesarrolladormas.ensamblador.funcs.DataSegmentAnalyzer.Symbol;

public class CodeSegmentAnalyzer {

    private static final String REG8 = "(AH|AL|BH|BL|CH|CL|DH|DL)";
    private static final String REG16 = "(AX|BX|CX|DX|SI|DI|BP|SP)";
    private static final String REG32 = "(EAX|EBX|ECX|EDX|ESI|EDI|EBP|ESP)";
    private static final String MEM = "\\[[0-9A-Fa-f]{1,8}h\\]";
    private static final String INM = "\\d+(h|b|o|q)?";
    private static final Pattern LABEL_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*:$");
    private static final List<String> RESERVED_WORDS = List.of(
            "add", "mov", "movzx", "or", "pop", "shr", "shl", "sub", "jmp", "jz", "main", "proc", "nop", "ends",
            ".data",
            ".code");

    // Patrones de las instrucciones seleccionadas
    private final Pattern CLD_PATTERN;
    private final Pattern CLI_PATTERN;

    private Set<String> declaredLabels; // Para almacenar las etiquetas declaradas

    public CodeSegmentAnalyzer(List<Symbol> vars) {
        // Patrones de las instrucciones válidas
        CLD_PATTERN = Pattern.compile("^CLD$", Pattern.CASE_INSENSITIVE); // Patrón para CLD
        CLI_PATTERN = Pattern.compile("^CLI$", Pattern.CASE_INSENSITIVE); // Patrón para CLI
        declaredLabels = new HashSet<>();
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

            if (line.startsWith(";") || line.isEmpty()) {
                continue;
            }

            if (line.contains(";")) {
                line = line.split(";", 2)[0].trim();
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
                        String label = line.substring(0, line.length() - 1);
                        if (RESERVED_WORDS.contains(label.toLowerCase())) {
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
                if (CLD_PATTERN.matcher(line).matches() || CLI_PATTERN.matcher(line).matches()) {
                    analysisResults.add(new String[] { line, "correcta" });
                } else {
                    analysisResults.add(new String[] { line, "incorrecta", "Error de sintaxis" });
                }
            }
        }

        return analysisResults;
    }

}

