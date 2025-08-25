package engine.api;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExecutionResult {
    private final int runNumber;
    private final int expansionLevel;
    private final List<Integer> inputs;
    private final int yValue;
    private final Map<String, Integer> inputVariables;
    private final Map<String, Integer> workingVariables;
    private final int totalCycles;
    private final List<SInstruction> executedInstructions;

    public ExecutionResult(int runNumber, int expansionLevel, List<Integer> inputs, 
                          int yValue, Map<String, Integer> inputVariables, 
                          Map<String, Integer> workingVariables,
                          int totalCycles, List<SInstruction> executedInstructions) {
        if (inputs == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }
        if (inputVariables == null) {
            throw new IllegalArgumentException("Input variables cannot be null");
        }
        if (workingVariables == null) {
            throw new IllegalArgumentException("Working variables cannot be null");
        }
        if (executedInstructions == null) {
            throw new IllegalArgumentException("Executed instructions cannot be null");
        }
        if (runNumber <= 0) {
            throw new IllegalArgumentException("Run number must be positive");
        }
        if (expansionLevel < 0) {
            throw new IllegalArgumentException("Expansion level cannot be negative");
        }
        if (totalCycles < 0) {
            throw new IllegalArgumentException("Total cycles cannot be negative");
        }

        this.runNumber = runNumber;
        this.expansionLevel = expansionLevel;
        this.inputs = List.copyOf(inputs);
        this.yValue = yValue;
        this.inputVariables = new LinkedHashMap<>(inputVariables);
        this.workingVariables = new LinkedHashMap<>(workingVariables);
        this.totalCycles = totalCycles;
        this.executedInstructions = List.copyOf(executedInstructions);
    }

    public int getRunNumber() {
        return runNumber;
    }

    public int getExpansionLevel() {
        return expansionLevel;
    }

    public List<Integer> getInputs() {
        return inputs;
    }

    public int getYValue() {
        return yValue;
    }

    public Map<String, Integer> getInputVariables() {
        return inputVariables;
    }

    public Map<String, Integer> getWorkingVariables() {
        return workingVariables;
    }

    public int getTotalCycles() {
        return totalCycles;
    }

    public List<SInstruction> getExecutedInstructions() {
        return executedInstructions;
    }

    @Override
    public String toString() {
        return String.format("ExecutionResult{runNumber=%d, expansionLevel=%d, inputs=%s, yValue=%d, totalCycles=%d}", 
                           runNumber, expansionLevel, inputs, yValue, totalCycles);
    }
}
