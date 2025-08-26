package engine.model.serialization;

import engine.api.ExecutionResult;
import engine.api.SProgram;
import engine.api.SystemState;
import java.util.List;

public class SystemStateImpl implements SystemState {
    private final SProgram currentProgram;
    private final List<ExecutionResult> executionHistory;
    private final int nextRunNumber;
    
    public SystemStateImpl(SProgram currentProgram, List<ExecutionResult> executionHistory, int nextRunNumber) {
        this.currentProgram = currentProgram;
        this.executionHistory = List.copyOf(executionHistory);
        this.nextRunNumber = nextRunNumber;
    }
    
    @Override
    public SProgram getCurrentProgram() {
        return currentProgram;
    }
    
    @Override
    public List<ExecutionResult> getExecutionHistory() {
        return executionHistory;
    }
    
    @Override
    public int getNextRunNumber() {
        return nextRunNumber;
    }
    
    @Override
    public boolean isProgramLoaded() {
        return currentProgram != null;
    }
}
