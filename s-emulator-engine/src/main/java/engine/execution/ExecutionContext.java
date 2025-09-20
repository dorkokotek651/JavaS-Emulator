package engine.execution;

import engine.api.SInstruction;
import engine.model.FunctionRegistry;
import engine.model.SEmulatorConstants;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExecutionContext {
    private final VariableManager variableManager;
    private int currentInstructionIndex;
    private int totalCycles;
    private boolean programTerminated;
    private String terminationReason;
    private Map<String, Integer> labelToIndexMap;
    private final List<SInstruction> executedInstructions;
    private String pendingJumpLabel;
    
    private boolean debugMode;
    private boolean pauseRequested;
    private Map<String, Integer> previousVariableState;
    private Map<String, Integer> changedVariables;
    
    private boolean virtualExecutionMode;
    
    private FunctionRegistry functionRegistry;

    public ExecutionContext() {
        this.variableManager = new VariableManager();
        this.currentInstructionIndex = 0;
        this.totalCycles = 0;
        this.programTerminated = false;
        this.terminationReason = null;
        this.labelToIndexMap = null;
        this.executedInstructions = new ArrayList<>();
        this.pendingJumpLabel = null;
        
        this.debugMode = false;
        this.pauseRequested = false;
        this.previousVariableState = new HashMap<>();
        this.changedVariables = new HashMap<>();
        
        this.virtualExecutionMode = false;
        
        this.functionRegistry = null;
    }

    public VariableManager getVariableManager() {
        return variableManager;
    }

    public int getCurrentInstructionIndex() {
        return currentInstructionIndex;
    }

    public void setCurrentInstructionIndex(int currentInstructionIndex) {
        if (currentInstructionIndex < 0) {
            throw new IllegalArgumentException("Instruction index cannot be negative: " + currentInstructionIndex);
        }
        this.currentInstructionIndex = currentInstructionIndex;
    }

    public int getTotalCycles() {
        return totalCycles;
    }

    public void addCycles(int cycles) {
        if (cycles < 0) {
            throw new IllegalArgumentException("Cycles cannot be negative: " + cycles);
        }
        this.totalCycles += cycles;
    }

    public boolean isProgramTerminated() {
        return programTerminated;
    }

    public void terminate(String reason) {
        this.programTerminated = true;
        this.terminationReason = reason != null ? reason : "Program terminated";
    }

    public String getTerminationReason() {
        return terminationReason;
    }

    public void incrementInstructionPointer() {
        this.currentInstructionIndex++;
    }

    public void jumpToLabel(String label) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Jump label cannot be null or empty");
        }
        
        String targetLabel = label.trim();
        
        if (SEmulatorConstants.EXIT_LABEL.equals(targetLabel)) {
            terminate("Program reached EXIT label");
            return;
        }
        
        if (labelToIndexMap != null && labelToIndexMap.containsKey(targetLabel)) {
            this.currentInstructionIndex = labelToIndexMap.get(targetLabel);
        } else {
            this.pendingJumpLabel = targetLabel;
        }
    }

    public void setLabelToIndexMap(Map<String, Integer> labelToIndexMap) {
        this.labelToIndexMap = labelToIndexMap;
    }

    public Map<String, Integer> getLabelToIndexMap() {
        return labelToIndexMap;
    }

    public String getPendingJumpLabel() {
        return pendingJumpLabel;
    }

    public void clearPendingJump() {
        this.pendingJumpLabel = null;
    }

    public List<SInstruction> getExecutedInstructions() {
        return List.copyOf(executedInstructions);
    }

    public void addExecutedInstruction(SInstruction instruction) {
        if (instruction != null) {
            executedInstructions.add(instruction);
        }
    }

    public void reset() {
        this.currentInstructionIndex = 0;
        this.totalCycles = 0;
        this.programTerminated = false;
        this.terminationReason = null;
        this.executedInstructions.clear();
        this.pendingJumpLabel = null;
        this.variableManager.reset();
    }

    public void initializeInputs(List<Integer> inputValues) {
        variableManager.initializeInputs(inputValues);
    }

    public void enableDebugMode() {
        this.debugMode = true;
        this.pauseRequested = false;
        takeVariableSnapshot();
    }
    
    public void disableDebugMode() {
        this.debugMode = false;
        this.pauseRequested = false;
        this.previousVariableState.clear();
        this.changedVariables.clear();
    }
    
    public boolean isDebugMode() {
        return debugMode;
    }
    
    public void enableVirtualExecutionMode() {
        this.virtualExecutionMode = true;
    }
    
    public void disableVirtualExecutionMode() {
        this.virtualExecutionMode = false;
    }
    
    public boolean isVirtualExecutionMode() {
        return virtualExecutionMode;
    }
    
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
    
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }
    
    public void requestPause() {
        if (debugMode) {
            this.pauseRequested = true;
        }
    }
    
    public void resume() {
        this.pauseRequested = false;
    }
    
    public boolean isPauseRequested() {
        return pauseRequested;
    }
    
    public void takeVariableSnapshot() {
        if (!debugMode) {
            return;
        }
        
        previousVariableState.clear();
        
        Map<String, Integer> inputVars = variableManager.getSortedInputVariablesMap();
        previousVariableState.putAll(inputVars);
        
        Map<String, Integer> workingVars = variableManager.getSortedWorkingVariablesMap();
        previousVariableState.putAll(workingVars);
        
        previousVariableState.put("y", variableManager.getYValue());
    }
    
    public void detectVariableChanges() {
        if (!debugMode) {
            return;
        }
        
        changedVariables.clear();
        
        Map<String, Integer> currentInputVars = variableManager.getSortedInputVariablesMap();
        for (Map.Entry<String, Integer> entry : currentInputVars.entrySet()) {
            String varName = entry.getKey();
            Integer currentValue = entry.getValue();
            Integer previousValue = previousVariableState.get(varName);
            
            if (previousValue == null || !previousValue.equals(currentValue)) {
                changedVariables.put(varName, currentValue);
            }
        }
        
        Map<String, Integer> currentWorkingVars = variableManager.getSortedWorkingVariablesMap();
        for (Map.Entry<String, Integer> entry : currentWorkingVars.entrySet()) {
            String varName = entry.getKey();
            Integer currentValue = entry.getValue();
            Integer previousValue = previousVariableState.get(varName);
            
            if (previousValue == null || !previousValue.equals(currentValue)) {
                changedVariables.put(varName, currentValue);
            }
        }
        
        int currentY = variableManager.getYValue();
        Integer previousY = previousVariableState.get("y");
        if (previousY == null || !previousY.equals(currentY)) {
            changedVariables.put("y", currentY);
        }
    }
    
    public Map<String, Integer> getChangedVariables() {
        return new HashMap<>(changedVariables);
    }

}
