package fx.util;

import engine.api.SProgram;
import engine.api.SInstruction;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class InputAnalyzer {
    
    private static final Pattern X_VARIABLE_PATTERN = Pattern.compile("x\\d+");

    public static List<String> getRequiredInputVariables(SProgram program) {
        if (program == null) {
            return new ArrayList<>();
        }

        Set<String> usedVariables = new HashSet<>();
        
        for (int i = 0; i < program.getInstructions().size(); i++) {
            SInstruction instruction = program.getInstructions().get(i);
            analyzeInstruction(instruction, usedVariables);
        }

        List<String> sortedVariables = new ArrayList<>(usedVariables);
        sortedVariables.sort((a, b) -> {
            int numA = extractVariableNumber(a);
            int numB = extractVariableNumber(b);
            return Integer.compare(numA, numB);
        });
        
        return sortedVariables;
    }

    private static void analyzeInstruction(SInstruction instruction, Set<String> usedVariables) {
        if (instruction == null) {
            return;
        }
        
        String variable = instruction.getVariable();
        if (variable != null && X_VARIABLE_PATTERN.matcher(variable).matches()) {
            usedVariables.add(variable);
        }
        
        for (String argValue : instruction.getArguments().values()) {
            if (argValue != null) {
                extractVariablesFromString(argValue, usedVariables);
            }
        }
    }

    private static void extractVariablesFromString(String text, Set<String> usedVariables) {
        Matcher matcher = X_VARIABLE_PATTERN.matcher(text);
        while (matcher.find()) {
            String foundVar = matcher.group();
            usedVariables.add(foundVar);
        }
    }

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

    public static int getMaxInputVariableNumber(SProgram program) {
        List<String> variables = getRequiredInputVariables(program);
        if (variables.isEmpty()) {
            return 0;
        }
        
        return extractVariableNumber(variables.get(variables.size() - 1));
    }

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
