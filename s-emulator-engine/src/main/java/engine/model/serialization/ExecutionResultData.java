package engine.model.serialization;

import java.util.List;
import java.util.Map;

public class ExecutionResultData {
    private int runNumber;
    private int expansionLevel;
    private List<Integer> inputs;
    private int yValue;
    private Map<String, Integer> inputVariables;
    private Map<String, Integer> workingVariables;
    private int totalCycles;
    
    public ExecutionResultData() {
    }
    
    public ExecutionResultData(int runNumber, int expansionLevel, List<Integer> inputs, 
                              int yValue, Map<String, Integer> inputVariables, 
                              Map<String, Integer> workingVariables, int totalCycles) {
        this.runNumber = runNumber;
        this.expansionLevel = expansionLevel;
        this.inputs = inputs;
        this.yValue = yValue;
        this.inputVariables = inputVariables;
        this.workingVariables = workingVariables;
        this.totalCycles = totalCycles;
    }
    
    public int getRunNumber() {
        return runNumber;
    }
    
    public void setRunNumber(int runNumber) {
        this.runNumber = runNumber;
    }
    
    public int getExpansionLevel() {
        return expansionLevel;
    }
    
    public void setExpansionLevel(int expansionLevel) {
        this.expansionLevel = expansionLevel;
    }
    
    public List<Integer> getInputs() {
        return inputs;
    }
    
    public void setInputs(List<Integer> inputs) {
        this.inputs = inputs;
    }
    
    public int getYValue() {
        return yValue;
    }
    
    public void setYValue(int yValue) {
        this.yValue = yValue;
    }
    
    public Map<String, Integer> getInputVariables() {
        return inputVariables;
    }
    
    public void setInputVariables(Map<String, Integer> inputVariables) {
        this.inputVariables = inputVariables;
    }
    
    public Map<String, Integer> getWorkingVariables() {
        return workingVariables;
    }
    
    public void setWorkingVariables(Map<String, Integer> workingVariables) {
        this.workingVariables = workingVariables;
    }
    
    public int getTotalCycles() {
        return totalCycles;
    }
    
    public void setTotalCycles(int totalCycles) {
        this.totalCycles = totalCycles;
    }
}
