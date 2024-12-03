package dev.undesarrolladormas.ensamblador.funcs;

import java.util.ArrayList;
import java.util.List;

public class Identify {
    private static final List<String> PALABRAS_RESERVADAS = List.of("FLAT", "STDCALL", "4096", "EXITPROCESS", "PROTO", "DWEXITCODE", "MAIN", "PROC", "INVOKE", "EXITPROCESS", "ENDP", "END", "?");
    private static final List<String> TIPOS_DATOS = List.of("BYTE","SBYTE","WORD","SWORD","DWORD","SDWORD", "FWORD", "QWORD", "TBYTE", "REAL4", "REAL8", "REAL10");
    private static final List<String> REGISTROS = List.of("EAX", "EBX", "ECX", "EDX", "ESI", "EDI", "ESP", "EBP","AX", "BX", "CX", "DX", "SI", "DI", "SP", "BP");
    private static final List<String> SIMBOLOS = List.of(
        "AAA", "AAS", "ADC", "ADD", "AND", "CALL", "CBW", "CLC", "CMC", "CMP", "CMPS", "DAA", "DAS", "DEC", "DIV", "ESC", "HLT", "IMUL", "IN", "INT", "INTO", "IRET",
        "JA", "JAE", "JG", "JGE", "JL", "JLE", "JMP", "JNA", "JNAE", "JNB", "JNBE", "JNC", "JNE", "JNG", "JNGE", "JNL", "JNO", "JNS", "JO", "JPE", "JPO", "JS", 
        "LAHF", "LDS", "LEA", "LES", "LOCK", "LODS", "LOOP", "LOOPNZ", "LOOPZ", "MOV", "MOVS", "NEG", "NOT", "OR", "OUT", "POP", "PUSH", "RCR", "REP", "RET", 
        "ROL", "ROR", "SAHF", "SAL", "SHL", "SHR", "SBB", "SCAS", "STC", "STD", "STI", "STOS", "SUB", "XLA", "XOR"
    );

    // Function to tokenize and return tokens in the correct order
    public static String[] getTokens(String str) {
        List<String> lines = List.of(str.split("\n"));
        List<String> tokens = new ArrayList<>();

        for (String line : lines) {
            line = line.trim();

            // Skip full-line comments and empty lines
            if (line.startsWith(";") || line.isEmpty()) {
                continue;
            }

            // Remove inline comments
            if (line.contains(";")) {
                line = line.split(";", 2)[0].trim();
            }

            // Tokenize the line
            String[] lineTokens = line.split("\\s+|,|:");
            for (String token : lineTokens) {
                if (!token.isEmpty()) {
                    tokens.add(token);
                }
            }
        }

        return tokens.toArray(String[]::new);
    }

    // Function to classify each token
    public static String classifyToken(String token) {
        String upperToken = token.toUpperCase();
        
        if (upperToken.startsWith(".")) {
            return "Directiva";
        } else if (REGISTROS.contains(upperToken)) {
            return "Registro";
        } else if (TIPOS_DATOS.contains(upperToken)) {
            return "Tipo de dato";
        } else if (PALABRAS_RESERVADAS.contains(upperToken)) {
            return "Palabra Reservada";
        } else if (SIMBOLOS.contains(upperToken)) {
            return "Símbolo";
        } else if (upperToken.matches("[01]+B")) {
            return "Constante Binaria";
        } else if (upperToken.matches("[0-7]+O")) {
            return "Constante Octal";
        } else if (upperToken.matches("[0-9]+")) {
            return "Constante Decimal";
        } else if (upperToken.matches("[0-9A-F]+H")) {
            return "Constante Hexadecimal";
        } else {
            return "Variable o símbolo";
        }
    }
}
