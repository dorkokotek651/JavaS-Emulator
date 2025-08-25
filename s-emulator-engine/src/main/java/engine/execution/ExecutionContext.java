package engine.execution;

import engine.api.SInstruction;
import engine.model.SEmulatorConstants;
import java.util.ArrayList;
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


    public ExecutionContext() {
        this.variableManager = new VariableManager();
        this.currentInstructionIndex = 0;
        this.totalCycles = 0;
        this.programTerminated = false;
        this.terminationReason = null;
        this.labelToIndexMap = null;
        this.executedInstructions = new ArrayList<>();
        this.pendingJumpLabel = null;
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


}
