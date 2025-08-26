package engine.api;

import java.util.List;

public interface SystemState {
    SProgram getCurrentProgram();
    
    List<ExecutionResult> getExecutionHistory();
    
    int getNextRunNumber();
    
    boolean isProgramLoaded();
}
