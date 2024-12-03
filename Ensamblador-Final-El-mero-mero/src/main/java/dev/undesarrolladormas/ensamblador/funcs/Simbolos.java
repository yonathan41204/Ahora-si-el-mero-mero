package dev.undesarrolladormas.ensamblador.funcs;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Simbolos {
    public static final String[] REG = { "AH", "AL", "BH", "BL", "CH", "CL", "DH", "DL", "AX", "BX", "CX", "DX", "SI",
            "DI", "BP", "SP", "EAX", "EBX", "ECX", "EDX", "ESI", "EDI", "EBP", "ESP" };
    public static final String[] ACUM = { "AL", "AX", "EAX" };

    private static final Set<String> REG_SET = new HashSet<>(Arrays.asList(REG));
    private static final Set<String> ACUM_SET = new HashSet<>(Arrays.asList(ACUM));

    public static boolean isRegister(String operand) {
        return REG_SET.contains(operand.toUpperCase());
    }

    public static boolean isAccumulator(String operand) {
        return ACUM_SET.contains(operand.toUpperCase());
    }
}