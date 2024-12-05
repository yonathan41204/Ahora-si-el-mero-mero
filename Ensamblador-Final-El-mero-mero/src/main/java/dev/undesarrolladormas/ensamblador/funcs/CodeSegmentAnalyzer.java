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
    private final Pattern JUMP_PATTERN;
    private static final List<String> JUMP_INSTRUCTIONS = List.of("jmp", "jz", "jnz", "je", "jne");

    // Patrones de las instrucciones seleccionadas
    private final Pattern CLD_PATTERN;
    private final Pattern CLI_PATTERN;
    private final Pattern NOP_PATTERN;
    private final Pattern POPA_PATTERN;
    private final Pattern AAD_PATTERN; // Patrón para AAD
    private final Pattern AAM_PATTERN; // Patrón para AAM
    private final Pattern MUL_PATTERN; // Patrón para MUL
    private final Pattern INC_PATTERN; // Patrón para INC
    private final Pattern IDIV_PATTERN; // Patrón para IDIV
    private final Pattern SAR_PATTERN; // Patrón para SAR
    private final Pattern TEST_PATTERN;
    private final Pattern RCL_PATTERN;
    private final Pattern XCHG_PATTERN;

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
        MUL_PATTERN = Pattern.compile(
                "^MUL\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+)$",
                Pattern.CASE_INSENSITIVE);
        INC_PATTERN = Pattern.compile(
                "^INC\\s+([a-zA-Z_][a-zA-Z0-9_]*|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+|\\[[^\\]]+\\])$",
                Pattern.CASE_INSENSITIVE); // Patrón para INC
        IDIV_PATTERN = Pattern.compile(
                "^IDIV\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+)$",
                Pattern.CASE_INSENSITIVE); // Patrón para IDIV
        SAR_PATTERN = Pattern.compile(
                "^SAR\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|[0-9]+),\\s*([a-zA-Z_][a-zA-Z0-9_]*|[0-9]+)$",
                Pattern.CASE_INSENSITIVE); // Patrón para SAR
        TEST_PATTERN = Pattern.compile(
                "^TEST\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+),\\s*([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP|[0-9]+)$",
                Pattern.CASE_INSENSITIVE);
        RCL_PATTERN = Pattern.compile(
                "^RCL\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP),\\s*([0-9]+|CL)$",
                Pattern.CASE_INSENSITIVE);
        XCHG_PATTERN = Pattern.compile(
                "^XCHG\\s+([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP),\\s*([a-zA-Z_][a-zA-Z0-9_]*|\\[[^\\]]+\\]|AL|AX|BX|CX|DX|SI|DI|BP|SP)$",
                Pattern.CASE_INSENSITIVE);
        JUMP_PATTERN = Pattern.compile("^(jmp|jz|jnz|je|jne)\\s+([a-zA-Z_][a-zA-Z0-9_]*)$", 
                Pattern.CASE_INSENSITIVE);

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
                        if (JUMP_PATTERN.matcher(line).matches()) {
                         String[] parts = line.split("\\s+");
                        String label = parts[1].toLowerCase();
                         referencedLabels.add(label);
                        if (RESERVED_WORDS.contains(label)) {
                            analysisResults.add(
                                    new String[] { line, "incorrecta", "Etiqueta no puede ser palabra reservada" });
                            if (!declaredLabels.contains(label)) {
                                    analysisResults.add(new String[] { line, "incorrecta", "Etiqueta no declarada" });
                        } else {
                            declaredLabels.add(label);
                            analysisResults.add(new String[] { line, "correcta" });
                        }
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", "Etiqueta no válida" });
                    }
                    } else {
                        analysisResults.add(new String[] { line, "correcta" });
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
                } else if (SAR_PATTERN.matcher(line).matches()) {
                    String validation = validateSAR(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
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
                } else if (line.toUpperCase().startsWith("IDIV")) {
                    String validation = validateIDIV(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
                } else if (line.toUpperCase().startsWith("TEST")) {
                    String validation = validateTEST(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
                } else if (line.toUpperCase().startsWith("RCL")) {
                    String validation = validateRCL(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
                } else if (line.toUpperCase().startsWith("XCHG")) {
                    String validation = validateXCHG(line);
                    if (validation.equals("correcta")) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", validation });
                    }
                }else {
                    analysisResults.add(new String[] { line, "incorrecta", "Error de sintaxis" });
                }
            }
        }

        return analysisResults;
    }

    private String validateSAR(String line) {
        if (!SAR_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica de SAR incorrecta
        }

        // Dividir los operandos
        String[] parts = line.split("\\s+", 2);
        if (parts.length != 2 || !parts[1].contains(",")) {
            return "Error de sintaxis"; // Debe tener dos operandos separados por coma
        }

        String[] operands = parts[1].split("\\s*,\\s*");
        if (operands.length != 2) {
            return "Error de sintaxis"; // Debe tener exactamente dos operandos
        }

        String destino = operands[0].toLowerCase();
        String fuente = operands[1].toLowerCase();

        // Validar destino
        if (!destino.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]") && !declaredVariables.contains(destino)) {
            return "Operando de destino inválido"; // Destino no es válido
        }

        // Validar fuente (inmediato o registro)
        if (!fuente.matches("[0-9]+|cl|bp|") && !declaredVariables.contains(fuente)) {
            return "Operando de fuente inválido"; // Fuente no es válida
        }

        return "correcta"; // Todo está correcto
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
            return "correcta"; // Operando válido
        }

        if (declaredVariables.contains(operand)) {
            return "correcta"; // Operando es una variable declarada
        }

        return "Operando inválido"; // Operando no declarado o no permitido
    }

    private String validateIDIV(String line) {
        if (!IDIV_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica de IDIV incorrecta
        }

        // Extraer el operando y verificar si es una variable declarada
        String[] parts = line.split("\\s+");
        if (parts.length != 2) {
            return "Error de sintaxis"; // Debe tener un solo operando
        }

        String operand = parts[1].toLowerCase();
        if (operand.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]|[0-9]+")) {
            return "correcta"; // Operando válido
        }

        if (declaredVariables.contains(operand)) {
            return "correcta"; // Operando es una variable declarada
        }

        return "Operando inválido"; // Operando no declarado o no permitido
    }

    private String validateTEST(String line) {
        if (!TEST_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica incorrecta
        }

        // Dividir los operandos
        String[] parts = line.split("\\s+", 2);
        if (parts.length != 2 || !parts[1].contains(",")) {
            return "Error de sintaxis"; // Debe tener dos operandos separados por coma
        }

        String[] operands = parts[1].split("\\s*,\\s*");
        if (operands.length != 2) {
            return "Error de sintaxis"; // Debe tener exactamente dos operandos
        }

        String destino = operands[0].toLowerCase();
        String fuente = operands[1].toLowerCase();

        // Validar operando de destino
        if (!destino.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]") && !declaredVariables.contains(destino)) {
            return "Operando de destino inválido"; // Destino no es válido
        }

        // Validar operando de fuente
        if (!fuente.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]|[0-9]+") && !declaredVariables.contains(fuente)) {
            return "Operando de fuente inválido"; // Fuente no es válida
        }

        return "correcta"; // Todo está correcto
    }

    private String validateRCL(String line) {
        if (!RCL_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica de RCL incorrecta
        }

        // Dividir los operandos
        String[] parts = line.split("\\s+", 2);
        if (parts.length != 2 || !parts[1].contains(",")) {
            return "Error de sintaxis"; // Debe tener dos operandos separados por coma
        }

        String[] operands = parts[1].split("\\s*,\\s*");
        if (operands.length != 2) {
            return "Error de sintaxis"; // Debe tener exactamente dos operandos
        }

        String destino = operands[0].toLowerCase();
        String fuente = operands[1].toLowerCase();

        // Validar operando de destino
        if (!destino.matches("al|ax|bx|cx|dx|si|di|bp|sp|\\[[^\\]]+\\]") && !declaredVariables.contains(destino)) {
            return "Operando inválido"; // Destino no es válido
        }

        // Validar operando de fuente
        if (!fuente.equals("cl") && !fuente.matches("[0-9]+")) {
            return "Operando inválido"; // Fuente no es válida
        }

        // Si es inmediato, validar que esté entre 1 y 31
        if (fuente.matches("[0-9]+")) {
            int valorInmediato = Integer.parseInt(fuente);
            if (valorInmediato < 1 || valorInmediato > 31) {
                return "Inmediato fuera de rango"; // Inmediato no válido
            }
        }

        return "correcta"; // Todo está correcto
    }

    private String validateXCHG(String line) {
        if (!XCHG_PATTERN.matcher(line).matches()) {
            return "Error de sintaxis"; // Sintaxis básica de XCHG incorrecta
        }

        // Dividir los operandos
        String[] parts = line.split("\\s+", 2);
        if (parts.length != 2 || !parts[1].contains(",")) {
            return "Error de sintaxis"; // Debe tener dos operandos separados por coma
        }

        String[] operands = parts[1].split("\\s*,\\s*");
        if (operands.length != 2) {
            return "Error de sintaxis"; // Debe tener exactamente dos operandos
        }

        String operando1 = operands[0].toLowerCase();
        String operando2 = operands[1].toLowerCase();

        // Validar que ambos operandos sean registros, memoria o variables declaradas
        boolean operando1EsMemoria = operando1.matches("\\[[^\\]]+\\]");
        boolean operando2EsMemoria = operando2.matches("\\[[^\\]]+\\]");
        boolean operando1EsRegistro = operando1.matches("al|ax|bx|cx|dx|si|di|bp|sp");
        boolean operando2EsRegistro = operando2.matches("al|ax|bx|cx|dx|si|di|bp|sp");

        // Operando 1 o 2 puede ser una variable declarada
        boolean operando1EsVariable = declaredVariables.contains(operando1);
        boolean operando2EsVariable = declaredVariables.contains(operando2);

        if (!(operando1EsMemoria || operando1EsRegistro || operando1EsVariable)) {
            return "Operando 1 inválido"; // Operando 1 no es válido
        }

        if (!(operando2EsMemoria || operando2EsRegistro || operando2EsVariable)) {
            return "Operando 2 inválido"; // Operando 2 no es válido
        }

        // Ambos operandos no pueden ser memoria
        if (operando1EsMemoria && operando2EsMemoria) {
            return "Ambos operandos no pueden ser direcciones de memoria";
        }

        return "correcta"; // Todo está correcto
    }

    

}
