import java.util.*;

class TwoPassAssembler {
    // Symbol Table
    static Map<String, Integer> symbolTable = new LinkedHashMap<>();
    
    // Intermediate Code
    static List<String> intermediateCode = new ArrayList<>();

    // Location Counter
    static int locCtr;

    public static void main(String[] args) {
        // Input Assembly Code
        String[] assemblyCode = {
            "START 100",
            "L1 MOVER AREG, A",
            "ADD BREG, A",
            "MOVER BREG, A",
            "ORIGIN L1",
            "MOVER BREG, A",
            "A DS 5",
            "B DC 5",
            "END"
        };

        // Process each line in Pass 1
        pass1(assemblyCode);

        // Print Symbol Table
        System.out.println("Symbol Table:");
        System.out.printf("%-10s %-10s\n", "Symbol", "Address");
        System.out.println("--------------------");
        for (Map.Entry<String, Integer> entry : symbolTable.entrySet()) {
            System.out.printf("%-10s %-10d\n", entry.getKey(), entry.getValue());
        }

        // Print Intermediate Code
        System.out.println("\nIntermediate Code:");
        System.out.printf("%-20s\n", "Code");
        System.out.println("--------------------");
        for (String line : intermediateCode) {
            System.out.printf("%-20s\n", line);
        }
    }

    public static void pass1(String[] code) {
        locCtr = 0;

        for (String line : code) {
            String[] tokens = line.split(" ");

            // Process START
            if (tokens[0].equals("START")) {
                locCtr = Integer.parseInt(tokens[1]);
                intermediateCode.add("(AD,01) (C," + locCtr + ")");
                continue;
            }

            // Check for labels
            if (!tokens[0].equals("END") && !isInstruction(tokens[0])) {
                String label = tokens[0];
                symbolTable.put(label, locCtr);

                // Process DS or DC directives
                if (tokens[1].equals("DS")) {
                    int size = Integer.parseInt(tokens[2]);
                    intermediateCode.add("(DL,02) (C," + size + ")");
                    locCtr += size;
                    continue;
                } else if (tokens[1].equals("DC")) {
                    int value = Integer.parseInt(tokens[2]);
                    intermediateCode.add("(DL,01) (C," + value + ")");
                    locCtr++;
                    continue;
                }
            }

            // Process ORIGIN
            if (tokens[0].equals("ORIGIN")) {
                String label = tokens[1];
                if (symbolTable.containsKey(label)) {
                    locCtr = symbolTable.get(label);
                    intermediateCode.add("(AD,03) (S," + getSymbolIndex(label) + ")");
                } else {
                    System.err.println("Error: Undefined symbol in ORIGIN - " + label);
                }
                continue;
            }

            // Process END
            if (tokens[0].equals("END")) {
                intermediateCode.add("(AD,02)");
                break;
            }

            // Process instructions (MOVER, ADD, etc.)
            if (isInstruction(tokens[0])) {
                String mnemonic = tokens[0];
                String register = tokens[1].replace(",", "");
                String operand = tokens[2];
                
                int mnemonicCode = mnemonic.equals("MOVER") ? 04 : 01;
                int registerCode = register.equals("AREG") ? 1 : 2;

                // Add operand to symbol table if not already present
                if (!symbolTable.containsKey(operand)) {
                    symbolTable.put(operand, -1); // Placeholder address
                }

                intermediateCode.add("(IS," + mnemonicCode + ") " + registerCode + ", (S," + getSymbolIndex(operand) + ")");
                locCtr++;
            }
        }
    }

    // Helper method to determine if a token is an instruction
    public static boolean isInstruction(String token) {
        return token.equals("MOVER") || token.equals("ADD") || token.equals("SUB") || token.equals("MULT") || token.equals("DIV");
    }

    // Helper method to get index of a symbol in Symbol Table
    public static int getSymbolIndex(String symbol) {
        int index = 0;
        for (String key : symbolTable.keySet()) {
            if (key.equals(symbol)) {
                return index;
            }
            index++;
        }
        return -1;
    }
}
