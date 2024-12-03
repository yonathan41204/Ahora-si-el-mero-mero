package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import dev.undesarrolladormas.ensamblador.funcs.DataSegmentAnalyzer.Symbol;

public class CodeSegmentAnalyzer {

    private static final String REG8 = "(AH|AL|BH|BL|CH|CL|DH|DL)";
    private static final String REG16 = "(AX|BX|CX|DX|SI|DI|BP|SP)";
    private static final String REG32 = "(EAX|EBX|ECX|EDX|ESI|EDI|EBP|ESP)";
    private static final String MEM = "\\[[0-9A-Fa-f]{1,8}h\\]";
    private static final String MEM16 = "\\[[0-9A-Fa-f]{1,4}h\\]";
    private static final String MEM8 = "\\[[0-9A-Fa-f]{1,2}h\\]";
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

    private int programCounter = 0x0250; // Contador de programa inicializado en 0250H
    private final List<Symbol> variables;

    public CodeSegmentAnalyzer(List<Symbol> vars) {
        this.variables = vars;

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
            "^mov\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + REGSEG + "|" + pattern.toString() + "),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + REGSEG + "|" + pattern.toString() + ")$",
            Pattern.CASE_INSENSITIVE
        );

        MOVZX_PATTERN = Pattern.compile(
            "^movzx\\s+(" + REG32 + "|" + REG16 + "),\\s*(" + REG16 + "|" + REG8 + "|" + MEM16 + "|" + MEM8 + "|" + pattern.toString() + ")$",
            Pattern.CASE_INSENSITIVE
        );

        OR_PATTERN = Pattern.compile(
            "^or\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() + "),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + ACUM + "|" + pattern.toString() + ")$",
            Pattern.CASE_INSENSITIVE
        );

        POP_PATTERN = Pattern.compile(
            "^pop\\s+(" + REG16 + "|" + REG32 + "|" + MEM16 + "|" + MEM + "|" + REGSEG + "|" + pattern.toString() + ")$",
            Pattern.CASE_INSENSITIVE
        );

        SHR_PATTERN = Pattern.compile(
            "^shr\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() + "),\\s*(CL|" + INM + ")$",
            Pattern.CASE_INSENSITIVE
        );

        SHL_PATTERN = Pattern.compile(
            "^shl\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() + "),\\s*(CL|" + INM + ")$",
            Pattern.CASE_INSENSITIVE
        );

        SUB_PATTERN = Pattern.compile(
            "^sub\\s+(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + pattern.toString() + "),\\s*(" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "|" + INM + "|" + ACUM + "|" + pattern.toString() + ")$",
            Pattern.CASE_INSENSITIVE
        );

    }

    public List<String[]> analyze(String assemblyCode) {
        List<String[]> analysisResults = new ArrayList<>();
        String[] lines = assemblyCode.split("\\n");
        boolean inCodeSegment = false;

        for (String line : lines) {
            line = line.trim();

            if (line.equalsIgnoreCase(".code segment") || line.equalsIgnoreCase(".code")) {
                inCodeSegment = true;
                programCounter = 0x0250; // Reinicia el contador al entrar al segmento de código
                analysisResults.add(new String[] { line, "correcta", String.format("%04XH", programCounter) });
                continue;
            } else if (line.equalsIgnoreCase("ends")) {
                if (inCodeSegment) {
                    inCodeSegment = false;
                    analysisResults.add(new String[] { line, "correcta", "" });
                }
                continue;
            }

            if (inCodeSegment) {
                if (isValidInstruction(line)) {
                    analysisResults.add(new String[] { line, "correcta", String.format("%04XH", programCounter) });
                    programCounter += calculateInstructionSize(line); // Incrementa el contador según el tamaño
                } else {
                    analysisResults.add(new String[] { line, "incorrecta", "" });
                }
            }
        }

        return analysisResults;
    }

    private boolean isValidInstruction(String line) {
        return ADD_PATTERN.matcher(line).matches() ||
               MOV_PATTERN.matcher(line).matches() ||
               MOVZX_PATTERN.matcher(line).matches() ||
               OR_PATTERN.matcher(line).matches() ||
               POP_PATTERN.matcher(line).matches() ||
               SHR_PATTERN.matcher(line).matches() ||
               SHL_PATTERN.matcher(line).matches() ||
               SUB_PATTERN.matcher(line).matches();
    }

    private int calculateInstructionSize(String line) {
        // Por simplicidad, asumiremos un tamaño fijo. Ajustar según sea necesario.
        return 3; // Ejemplo: 3 bytes por instrucción
    }

    public int getProgramCounter() {
        return programCounter;
    }
}
