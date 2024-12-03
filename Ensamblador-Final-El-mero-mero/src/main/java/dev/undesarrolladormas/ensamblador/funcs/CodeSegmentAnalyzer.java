package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import dev.undesarrolladormas.ensamblador.funcs.DataSegmentAnalyzer.Symbol;

public class CodeSegmentAnalyzer {

    private static final String REG8 = "(AH|AL|BH|BL|CH|CL|DH|DL)";
    private static final String REG16 = "(AX|BX|CX|DX|SI|DI|BP|SP)";
    private static final String REG32 = "(EAX|EBX|ECX|EDX|ESI|EDI|EBP|ESP)";
    private static final String MEM = "\\[[0-9A-Fa-f]{1,8}h\\]";
    private static final String MEM16 = "\\[[0-9A-Fa-f]{1,4}h\\]"; // Asumimos que las direcciones de memoria son de 16 bits
    private static final String MEM8 = "\\[[0-9A-Fa-f]{1,2}h\\]"; // Asumimos que las direcciones de memoria son de 8 bits
    private static final String ACUM = "(AL|AX|EAX)";
    private static final String INM = "\\d+(h|b|o|q)?";
    private static final String REGSEG = "(CS|DS|ES|SS|FS|GS)";

    private final Pattern ADD_PATTERN;
    private final Pattern MOV_PATTERN;
    private final Pattern MOVZX_PATTERN;
    private final Pattern OR_PATTERN;
    private final Pattern POP_PATTERN;
    private final Pattern SHR_PATTERN;
    private final Pattern SHL_PATTERN;
    private final Pattern SUB_PATTERN;
    
    public CodeSegmentAnalyzer(List<Symbol> vars) {
        var pattern = new StringBuilder();
        for (int i = 0; i < vars.size(); i++) {
            var var = vars.get(i); 
            pattern.append(var.getName());
            if (i < vars.size() - 1) {
                pattern.append('|');
            }
        }
        
        ADD_PATTERN = Pattern.compile(
            "^add\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() + "),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + ACUM + "|" + pattern.toString() + ")$",
            Pattern.CASE_INSENSITIVE
        );

        MOV_PATTERN = Pattern.compile(
            "^mov\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + REGSEG + "|" + pattern.toString() +"),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + REGSEG + "|" + pattern.toString() +")$",
            Pattern.CASE_INSENSITIVE
        );
        
        MOVZX_PATTERN = Pattern.compile(
            "^movzx\\s+(" + REG32 + "|" + REG16 + "),\\s*(" + REG16 + "|" + REG8 + "|" + MEM16 + "|" + MEM8 + "|" + pattern.toString() +")$",
            Pattern.CASE_INSENSITIVE
        );

        OR_PATTERN = Pattern.compile(
            "^or\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() +"),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + ACUM + "|" + pattern.toString() +")$",
            Pattern.CASE_INSENSITIVE
        );
    
        POP_PATTERN = Pattern.compile(
            "^pop\\s+(" + REG16 + "|" + REG32 + "|" + MEM16 + "|" + MEM + "|" + REGSEG + "|" + pattern.toString() +")$",
            Pattern.CASE_INSENSITIVE
        );

        SHR_PATTERN = Pattern.compile(
            "^shr\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() +"),\\s*(CL|" + INM + ")$",
            Pattern.CASE_INSENSITIVE
        );

        SHL_PATTERN = Pattern.compile(
            "^shl\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() +"),\\s*(CL|" + INM + ")$",
            Pattern.CASE_INSENSITIVE
        );

        SUB_PATTERN = Pattern.compile(
            "^sub\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() +"),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + ACUM + "|" + pattern.toString() +")$",
            Pattern.CASE_INSENSITIVE
        );

    } 

    private static final Pattern JZ_PATTERN = Pattern.compile(
        "^jz\\s+\\w+$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NOP_PATTERN = Pattern.compile(
        "^nop$",
        Pattern.CASE_INSENSITIVE
    );

    public List<String[]> analyze(String assemblyCode) {
        List<String[]> analysisResults = new ArrayList<>();
        String[] lines = assemblyCode.split("\\n");
        boolean inCodeSegment = false;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty()) {
                continue; // Omitir líneas vacías
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
                if (line.equalsIgnoreCase("main proc") || line.toLowerCase().startsWith("invoke")) {
                    continue; // Omitir líneas que contienen "main proc" o que comienzan con "invoke"
                }

                if (line.equalsIgnoreCase("nop")) {
                    if (NOP_PATTERN.matcher(line).matches()) {
                        analysisResults.add(new String[] { line, "correcta" });
                    }
                } else if (line.toLowerCase().startsWith("add")) {
                    if (isValidInstruction(line, ADD_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("mov")) {
                    if (isValidInstruction(line, MOV_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("movzx")) {
                    if (isValidInstruction(line, MOVZX_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("or")) {
                    if (isValidInstruction(line, OR_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("pop")) {
                    if (isValidInstruction(line, POP_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("shr")) {
                    if (isValidInstruction(line, SHR_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("shl")) {
                    if (isValidInstruction(line, SHL_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("sub")) {
                    if (isValidInstruction(line, SUB_PATTERN)) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else if (line.toLowerCase().startsWith("jz")) {
                    if (JZ_PATTERN.matcher(line).matches()) {
                        analysisResults.add(new String[] { line, "correcta" });
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta" });
                    }
                } else {
                    analysisResults.add(new String[] { line, "incorrecta" });
                }
            }
        }

        return analysisResults;
    }

    private boolean isValidInstruction(String line, Pattern pattern) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.matches()) {
            if (pattern == POP_PATTERN) {
                return true; // No need to check operand sizes for POP
            }
            String operand1 = matcher.group(1);
            String operand2 = matcher.groupCount() > 1 ? matcher.group(2) : null;

            if (operand1 != null) operand1 = operand1.trim();
            if (operand2 != null) operand2 = operand2.trim();

            // Verificar que ambos operandos sean del mismo tamaño
            return operand2 == null || areOperandsSameSize(operand1, operand2);
        }
        return false;
    }

    private boolean areOperandsSameSize(String operand1, String operand2) {
        int size1 = getOperandSize(operand1);
        int size2 = getOperandSize(operand2);
        return size1 == size2;
    }

    private int getOperandSize(String operand) {
        if (operand.matches(REG8)) {
            return 8;
        } else if (operand.matches(REG16)) {
            return 16;
        } else if (operand.matches(REG32)) {
            return 32;
        } else if (operand.matches(MEM)) {
            // Asumimos que las direcciones de memoria son de 32 bits
            return 32;
        } else if (operand.matches(MEM16)) {
            return 16;
        } else if (operand.matches(MEM8)) {
            return 8;
        } else if (operand.matches(ACUM)) {
            return getRegisterSize(operand);
        } else if (operand.matches(INM)) {
            // Asumimos que los valores inmediatos son de 32 bits
            return 32;
        } else if (operand.matches(REGSEG)) {
            return 16;
        }
        return 0;
    }

    private int getRegisterSize(String register) {
        return switch (register.toUpperCase()) {
            case "AH", "AL", "BH", "BL", "CH", "CL", "DH", "DL" -> 8;
            case "AX", "BX", "CX", "DX", "SI", "DI", "BP", "SP" -> 16;
            case "EAX", "EBX", "ECX", "EDX", "ESI", "EDI", "EBP", "ESP" -> 32;
            default -> 0;
        };
    }
}