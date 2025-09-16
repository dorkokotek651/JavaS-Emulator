package engine.execution;

import engine.model.SEmulatorConstants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class VariableManager {
    private final Map<String, Integer> variables;

    public VariableManager() {
        this.variables = new HashMap<>();
    }

    public int getValue(String variableName) {
        if (variableName == null || variableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        return variables.getOrDefault(variableName.trim(), 0);
    }

    public void setValue(String variableName, int value) {
        if (variableName == null || variableName.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable name cannot be null or empty");
        }
        if (value < 0) {
            throw new IllegalArgumentException("Variable value cannot be negative: " + value);
        }
        String trimmedName = variableName.trim();
        variables.put(trimmedName, value);
    }

    public void increment(String variableName) {
        setValue(variableName, getValue(variableName) + 1);
    }

    public void decrement(String variableName) {
        int currentValue = getValue(variableName);
        setValue(variableName, Math.max(0, currentValue - 1));
    }

    public List<String> getInputVariables() {
        return variables.keySet().stream()
                .filter(name -> SEmulatorConstants.X_VARIABLE_PATTERN.matcher(name).matches())
                .sorted((a, b) -> {
                    int numA = Integer.parseInt(a.substring(1));
                    int numB = Integer.parseInt(b.substring(1));
                    return Integer.compare(numA, numB);
                })
                .collect(Collectors.toList());
    }

    public List<String> getWorkingVariables() {
        return variables.keySet().stream()
                .filter(name -> SEmulatorConstants.Z_VARIABLE_PATTERN.matcher(name).matches())
                .sorted((a, b) -> {
                    int numA = Integer.parseInt(a.substring(1));
                    int numB = Integer.parseInt(b.substring(1));
                    return Integer.compare(numA, numB);
                })
                .collect(Collectors.toList());
    }

    public int getYValue() {
        return getValue(SEmulatorConstants.RESULT_VARIABLE);
    }

    public Map<String, Integer> getSortedInputVariablesMap() {
        List<String> sortedInputVariables = getInputVariables();
        Map<String, Integer> inputVars = new LinkedHashMap<>();
        sortedInputVariables.forEach(var -> inputVars.put(var, getValue(var)));

        return inputVars;
    }

    public Map<String, Integer> getSortedWorkingVariablesMap() {
        List<String> sortedWorkingVariables = getWorkingVariables();
        Map<String, Integer> workingVars = new LinkedHashMap<>();
        sortedWorkingVariables.forEach(var -> workingVars.put(var, getValue(var)));

        return workingVars;
    }

    public void initializeInputs(List<Integer> inputValues) {
        if (inputValues == null) {
            throw new IllegalArgumentException("Input values cannot be null");
        }
        
        for (int i = 0; i < inputValues.size(); i++) {
            String variableName = "x" + (i + 1);
            int value = inputValues.get(i);
            setValue(variableName, value);
        }
    }

    public void reset() {
        variables.clear();
    }
}
