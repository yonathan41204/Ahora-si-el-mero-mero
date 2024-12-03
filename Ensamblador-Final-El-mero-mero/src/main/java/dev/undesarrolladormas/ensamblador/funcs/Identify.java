package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.List;

public class Identify {
    // Define sets or lists for each type of token classification0
    private static final List<String> PALABRAS_RESERVADAS = List.of("FLAT", "STDCALL", "4096", "EXITPROCESS", "PROTO", "DWEXITCODE", "MAIN", "PROC", "INVOKE", "EXITPROCESS", "ENDP", "END", "?");
    private static final List<String> TIPOS_DATOS = List.of("BYTE","SBYTE","WORD","SWORD","DWORD","SDWORD", "FWORD", "QWORD", "TBYTE", "REAL4", "REAL8", "REAL10");
    private static final List<String> NEMONICOS = List.of("CLD", "CLI", "NOP", "POPA", "AAD", "AAM", "MUL", "INC", "IDIV", "SAR", "TEST", "RCL", "XCHG", "JB", "JE", "JNLE", "JNP", "JP", "JCXZ", "JZ");
    private static final List<String> REGISTROS = List.of("EAX", "EBX", "ECX", "EDX", "ESI", "EDI", "ESP", "EBP","AX", "BX", "CX", "DX", "SI", "DI", "SP", "BP");

    // Function to tokenize and return tokens in the correct order
    public static String[] getTokens(String str) {
        // Split the string by line and process each line
        List<String> lines = List.of(str.split("\n"));
        List<String> tokens = new ArrayList<>();

        for (String line : lines) {
            line = line.trim(); // Remove leading and trailing spaces
            if (line.isEmpty() || line.startsWith(";")) continue; // Skip empty lines and comments
            String[] lineTokens = line.split("\\s+|,|:"); // Split by spaces and commas
            for (String token : lineTokens) {
                if (!token.isEmpty()) tokens.add(token);
            }
        }
        
        return tokens.toArray(String[]::new);
    }

    // Function to classify each token
    public static String classifyToken(String token) {
        String upperToken = token.toUpperCase();
        
        if (upperToken.startsWith(".")){
        return "Directiva";
    } else if (NEMONICOS.contains(upperToken)) {
        return "Instruccion";
    } else if (REGISTROS.contains(upperToken)) {
        return "Registro";
    } else if (TIPOS_DATOS.contains(upperToken)) {
        return "Tipo de dato";
    } else if (PALABRAS_RESERVADAS.contains(upperToken)) {
        return "Palabra Reservada";
    } else if (upperToken.matches("[01]+B")){
        return "Constante Binaria";
    } else if (upperToken.matches("[0-7]+O")) {
        return "Constante Octal";
    } else if (upperToken.matches("[0-9]+")){
        return "Constante Decimal";
    } else if (upperToken.matches("[0-9A-F]+H")){
        return "Constante Hexadecimal";
    }
    else {
        return "Variable o símbolo";
    }      
    }
}