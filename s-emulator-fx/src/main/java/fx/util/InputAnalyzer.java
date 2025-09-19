package fx.util;

import engine.api.SProgram;
import engine.api.SInstruction;
import engine.model.SEmulatorConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Utility class for analyzing program input requirements.
 * Determines which Xi variables a program actually uses.
 */
public class InputAnalyzer {
    
    private static final Pattern X_VARIABLE_PATTERN = Pattern.compile("x\\d+");
    
    /**
     * Analyzes a program to determine which Xi variables it uses.
     * 
     * @param program The program to analyze
     * @return List of Xi variable names (e.g., ["x1", "x3", "x5"]) in ascending order
     */
    public static List<String> getRequiredInputVariables(SProgram program) {
        if (program == null) {
            System.out.println("InputAnalyzer: Program is null");
            return new ArrayList<>();
        }
        
        System.out.println("InputAnalyzer: Analyzing program: " + program.getName());
        System.out.println("InputAnalyzer: Program has " + program.getInstructions().size() + " instructions");
        
        Set<String> usedVariables = new HashSet<>();
        
        // Analyze all instructions in the program
        for (int i = 0; i < program.getInstructions().size(); i++) {
            SInstruction instruction = program.getInstructions().get(i);
            System.out.println("InputAnalyzer: Analyzing instruction " + (i+1) + ": " + instruction.getName() + 
                " variable=" + instruction.getVariable() + " args=" + instruction.getArguments());
            analyzeInstruction(instruction, usedVariables);
        }
        
        System.out.println("InputAnalyzer: Found variables: " + usedVariables);
        
        // Convert to sorted list
        List<String> sortedVariables = new ArrayList<>(usedVariables);
        sortedVariables.sort((a, b) -> {
            // Extract numeric part for proper sorting (x1, x2, x10, x11, etc.)
            int numA = extractVariableNumber(a);
            int numB = extractVariableNumber(b);
            return Integer.compare(numA, numB);
        });
        
        System.out.println("InputAnalyzer: Final sorted variables: " + sortedVariables);
        return sortedVariables;
    }
    
    /**
     * Analyzes a single instruction for Xi variable usage.
     */
    private static void analyzeInstruction(SInstruction instruction, Set<String> usedVariables) {
        if (instruction == null) {
            return;
        }
        
        // Check the instruction's variable
        String variable = instruction.getVariable();
        if (variable != null && X_VARIABLE_PATTERN.matcher(variable).matches()) {
            usedVariables.add(variable);
        }
        
        // Check all argument values for Xi variables
        for (String argValue : instruction.getArguments().values()) {
            if (argValue != null) {
                extractVariablesFromString(argValue, usedVariables);
            }
        }
    }
    
    /**
     * Extracts Xi variables from a string (e.g., "x1,x2" or "x1 + x3").
     */
    private static void extractVariablesFromString(String text, Set<String> usedVariables) {
        System.out.println("InputAnalyzer: Extracting variables from string: '" + text + "'");
        Matcher matcher = X_VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            String foundVar = matcher.group();
            System.out.println("InputAnalyzer: Found variable: " + foundVar);
            usedVariables.add(foundVar);
        }
    }
    
    /**
     * Extracts the numeric part from a variable name (e.g., "x5" -> 5).
     */
    private static int extractVariableNumber(String variableName) {
        if (variableName == null || !variableName.startsWith("x")) {
            return 0;
        }
        
        try {
            return Integer.parseInt(variableName.substring(1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    /**
     * Determines the maximum Xi variable number used in a program.
     * 
     * @param program The program to analyze
     * @return The highest Xi variable number (e.g., 5 for x1, x3, x5)
     */
    public static int getMaxInputVariableNumber(SProgram program) {
        List<String> variables = getRequiredInputVariables(program);
        if (variables.isEmpty()) {
            return 0;
        }
        
        return extractVariableNumber(variables.get(variables.size() - 1));
    }
    
    /**
     * Creates a complete input array with values for all Xi variables up to the maximum used.
     * Missing variables are filled with 0.
     * 
     * @param program The program to analyze
     * @param providedInputs Map of variable name to value (e.g., {"x1": 5, "x3": 10})
     * @return Complete input array [x1, x2, x3, ...] with missing values filled with 0
     */
    public static List<Integer> createCompleteInputArray(SProgram program, java.util.Map<String, Integer> providedInputs) {
        int maxVarNumber = getMaxInputVariableNumber(program);
        List<Integer> completeInputs = new ArrayList<>();
        
        for (int i = 1; i <= maxVarNumber; i++) {
            String varName = "x" + i;
            Integer value = providedInputs.get(varName);
            completeInputs.add(value != null ? value : 0);
        }
        
        return completeInputs;
    }
}
