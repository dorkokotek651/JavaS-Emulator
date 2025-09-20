package engine.expansion;

import engine.model.FunctionRegistry;
import engine.model.SEmulatorConstants;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ExpansionContext {
    private final LabelManager labelManager;
    private int currentLevel;
    private final Map<String, String> labelMappings;
    private final Map<String, String> variableMappings;
    private final Set<String> usedWorkingVariables;
    private int workingVariableCounter;
    private int currentOriginalLineNumber;
    private FunctionRegistry functionRegistry;

    public ExpansionContext() {
        this.labelManager = new LabelManager();
        this.currentLevel = 0;
        this.labelMappings = new HashMap<>();
        this.variableMappings = new HashMap<>();
        this.usedWorkingVariables = new HashSet<>();
        this.workingVariableCounter = 1;
        this.currentOriginalLineNumber = -1;
    }

    public ExpansionContext(Set<String> existingLabels, Set<String> existingVariables) {
        this.labelManager = new LabelManager(existingLabels);
        this.currentLevel = 0;
        this.labelMappings = new HashMap<>();
        this.variableMappings = new HashMap<>();
        this.usedWorkingVariables = new HashSet<>(existingVariables);
        this.workingVariableCounter = findNextWorkingVariableCounter(existingVariables);
        this.currentOriginalLineNumber = -1;
    }

    public String getUniqueLabel() {
        return labelManager.generateUniqueLabel();
    }

    public String getUniqueWorkingVariable() {
        String variable;
        do {
            variable = "z" + workingVariableCounter;
            workingVariableCounter++;
        } while (usedWorkingVariables.contains(variable));
        
        usedWorkingVariables.add(variable);
        return variable;
    }

    public void mapLabel(String original, String replacement) {
        if (original == null || original.trim().isEmpty()) {
            throw new IllegalArgumentException("Original label cannot be null or empty");
        }
        if (replacement == null || replacement.trim().isEmpty()) {
            throw new IllegalArgumentException("Replacement label cannot be null or empty");
        }
        labelMappings.put(original.trim(), replacement.trim());
    }

    public void mapVariable(String original, String replacement) {
        if (original == null || original.trim().isEmpty()) {
            throw new IllegalArgumentException("Original variable cannot be null or empty");
        }
        if (replacement == null || replacement.trim().isEmpty()) {
            throw new IllegalArgumentException("Replacement variable cannot be null or empty");
        }
        variableMappings.put(original.trim(), replacement.trim());
    }

    public String getMappedLabel(String original) {
        if (original == null) {
            return null;
        }
        return labelMappings.getOrDefault(original.trim(), original.trim());
    }

    public String getMappedVariable(String original) {
        if (original == null) {
            return null;
        }
        return variableMappings.getOrDefault(original.trim(), original.trim());
    }

    public int getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(int currentLevel) {
        if (currentLevel < 0) {
            throw new IllegalArgumentException("Current level cannot be negative: " + currentLevel);
        }
        this.currentLevel = currentLevel;
    }

    public void incrementLevel() {
        this.currentLevel++;
    }

    public Map<String, String> getLabelMappings() {
        return Map.copyOf(labelMappings);
    }

    public Map<String, String> getVariableMappings() {
        return Map.copyOf(variableMappings);
    }

    public void markLabelAsUsed(String label) {
        if (label != null && !label.trim().isEmpty()) {
            labelManager.markLabelAsUsed(label.trim());
        }
    }

    public void markVariableAsUsed(String variable) {
        if (variable != null && !variable.trim().isEmpty()) {
            usedWorkingVariables.add(variable.trim());
        }
    }

    public ExpansionContext createChildContext() {
        ExpansionContext child = new ExpansionContext();
        child.labelManager.copyUsedLabelsFrom(this.labelManager);
        child.usedWorkingVariables.addAll(this.usedWorkingVariables);
        child.workingVariableCounter = this.workingVariableCounter;
        child.currentLevel = this.currentLevel + 1;
        return child;
    }

    private int findNextWorkingVariableCounter(Set<String> existingVariables) {
        int maxCounter = 0;
        for (String variable : existingVariables) {
            if (SEmulatorConstants.Z_VARIABLE_PATTERN.matcher(variable).matches()) {
                try {
                    int counter = Integer.parseInt(variable.substring(1));
                    maxCounter = Math.max(maxCounter, counter);
                } catch (NumberFormatException e) {
                }
            }
        }
        return maxCounter + 1;
    }

    public int getCurrentOriginalLineNumber() {
        return currentOriginalLineNumber;
    }

    public void setCurrentOriginalLineNumber(int currentOriginalLineNumber) {
        this.currentOriginalLineNumber = currentOriginalLineNumber;
    }
    
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }
    
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
}
