package engine.model.serialization;

import java.util.List;

public class SystemStateData {
    private ProgramStateData program;
    private List<ExecutionResultData> executionHistory;
    private int nextRunNumber;
    
    public SystemStateData() {
    }
    
    public SystemStateData(ProgramStateData program, List<ExecutionResultData> executionHistory, int nextRunNumber) {
        this.program = program;
        this.executionHistory = executionHistory;
        this.nextRunNumber = nextRunNumber;
    }
    
    public ProgramStateData getProgram() {
        return program;
    }
    
    public void setProgram(ProgramStateData program) {
        this.program = program;
    }
    
    public List<ExecutionResultData> getExecutionHistory() {
        return executionHistory;
    }
    
    public void setExecutionHistory(List<ExecutionResultData> executionHistory) {
        this.executionHistory = executionHistory;
    }
    
    public int getNextRunNumber() {
        return nextRunNumber;
    }
    
    public void setNextRunNumber(int nextRunNumber) {
        this.nextRunNumber = nextRunNumber;
    }
}
