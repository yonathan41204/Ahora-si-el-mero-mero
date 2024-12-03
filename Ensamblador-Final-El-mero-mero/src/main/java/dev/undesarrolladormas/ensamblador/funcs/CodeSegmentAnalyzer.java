package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
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
    private static final Pattern LABEL_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*:$");
    private static final List<String> RESERVED_WORDS = List.of(
            "add", "mov", "movzx", "or", "pop", "shr", "shl", "sub", "jmp", "jz", "main", "proc", "nop", "ends", ".data",
            ".code");

    private final Pattern LD_PATTERN;
    private final Pattern CLI_PATTERN;
    private final Pattern NOP_PATTERN;
    private final Pattern POPA_PATTERN;
    private final Pattern AAD_PATTERN;
    private final Pattern AAM_PATTERN;
    private final Pattern MUL_PATTERN;
    private final Pattern INC_PATTERN;
    private final Pattern IDIV_PATTERN;
    private final Pattern SAR_PATTERN;
    private final Pattern TEST_PATTERN;
    private final Pattern RCL_PATTERN;
    private final Pattern XCHG_PATTERN;
    private final Pattern JB_PATTERN;
    private final Pattern JE_PATTERN;
    private final Pattern JNLE_PATTERN;
    private final Pattern JNP_PATTERN;
    private final Pattern JP_PATTERN;
    private final Pattern JCXZ_PATTERN;
    private final Pattern JZ_PATTERN;
    private final Pattern INT_PATTERN;

    private Set<String> declaredLabels;

    public CodeSegmentAnalyzer(List<Symbol> vars) {
        LD_PATTERN = Pattern.compile("^LD\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "$", Pattern.CASE_INSENSITIVE);
        CLI_PATTERN = Pattern.compile("^CLI$", Pattern.CASE_INSENSITIVE);
        NOP_PATTERN = Pattern.compile("^NOP$", Pattern.CASE_INSENSITIVE);
        POPA_PATTERN = Pattern.compile("^POPA$", Pattern.CASE_INSENSITIVE);
        AAD_PATTERN = Pattern.compile("^AAD$", Pattern.CASE_INSENSITIVE);
        AAM_PATTERN = Pattern.compile("^AAM$", Pattern.CASE_INSENSITIVE);
        MUL_PATTERN = Pattern.compile("^MUL\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "$", Pattern.CASE_INSENSITIVE);
        INC_PATTERN = Pattern.compile("^INC\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "$", Pattern.CASE_INSENSITIVE);
        IDIV_PATTERN = Pattern.compile("^IDIV\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "$", Pattern.CASE_INSENSITIVE);
        SAR_PATTERN = Pattern.compile("^SAR\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "$", Pattern.CASE_INSENSITIVE);
        TEST_PATTERN = Pattern.compile("^TEST\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "|" + MEM + "$", Pattern.CASE_INSENSITIVE);
        RCL_PATTERN = Pattern.compile("^RCL\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "$", Pattern.CASE_INSENSITIVE);
        XCHG_PATTERN = Pattern.compile("^XCHG\\s+" + REG8 + "|" + REG16 + "|" + REG32 + "$", Pattern.CASE_INSENSITIVE);
        JB_PATTERN = Pattern.compile("^JB\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        JE_PATTERN = Pattern.compile("^JE\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        JNLE_PATTERN = Pattern.compile("^JNLE\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        JNP_PATTERN = Pattern.compile("^JNP\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        JP_PATTERN = Pattern.compile("^JP\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        JCXZ_PATTERN = Pattern.compile("^JCXZ\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        JZ_PATTERN = Pattern.compile("^JZ\\s+[a-zA-Z_][a-zA-Z0-9_]*$", Pattern.CASE_INSENSITIVE);
        INT_PATTERN = Pattern.compile("^INT\\s+" + INM + "$", Pattern.CASE_INSENSITIVE);

        declaredLabels = new HashSet<>();
    }

    public List<String[]> analyze(String assemblyCode) {
        List<String[]> analysisResults = new ArrayList<>();
        String[] lines = assemblyCode.split("\\n");
        boolean inCodeSegment = false;

        for (String line : lines) {
            line = line.trim();

            if (line.isEmpty() || line.startsWith(";")) {
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
                            analysisResults.add(new String[] { line, "incorrecta", "Etiqueta no puede ser palabra reservada" });
                        } else {
                            declaredLabels.add(label);
                            analysisResults.add(new String[] { line, "correcta" });
                        }
                    } else {
                        analysisResults.add(new String[] { line, "incorrecta", "Etiqueta no válida" });
                    }
                    continue;
                }

                if (LD_PATTERN.matcher(line).matches() || CLI_PATTERN.matcher(line).matches() ||
                    NOP_PATTERN.matcher(line).matches() || POPA_PATTERN.matcher(line).matches() ||
                    AAD_PATTERN.matcher(line).matches() || AAM_PATTERN.matcher(line).matches() ||
                    MUL_PATTERN.matcher(line).matches() || INC_PATTERN.matcher(line).matches() ||
                    IDIV_PATTERN.matcher(line).matches() || SAR_PATTERN.matcher(line).matches() ||
                    TEST_PATTERN.matcher(line).matches() || RCL_PATTERN.matcher(line).matches() ||
                    XCHG_PATTERN.matcher(line).matches()) {

                    analysisResults.add(new String[] { line, "correcta" });

                } else if (JB_PATTERN.matcher(line).matches() || JE_PATTERN.matcher(line).matches() ||
                           JNLE_PATTERN.matcher(line).matches() || JNP_PATTERN.matcher(line).matches() ||
                           JP_PATTERN.matcher(line).matches() || JCXZ_PATTERN.matcher(line).matches() ||
                           JZ_PATTERN.matcher(line).matches()) {

                    String label = extractLabelFromJump(line);
                    if (!declaredLabels.contains(label)) {
                        analysisResults.add(new String[] { line, "incorrecta", "Etiqueta no declarada" });
                    } else {
                        analysisResults.add(new String[] { line, "correcta" });
                    }

                } else if (INT_PATTERN.matcher(line).matches()) {
                    analysisResults.add(new String[] { line, "correcta" });

                } else {
                    analysisResults.add(new String[] { line, "incorrecta", "No válida" });
                }
            }
        }

        return analysisResults;
    }

    private String extractLabelFromJump(String line) {
        Matcher matcher = Pattern.compile("\\s+([a-zA-Z_][a-zA-Z0-9_]*)").matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
}
