package engine.api;

import engine.exception.SProgramException;
import engine.exception.StateSerializationException;
import engine.execution.ExecutionContext;
import java.util.List;
import java.util.Map;

public interface SEmulatorEngine {
    void loadProgram(String xmlFilePath) throws SProgramException;
    
    SProgram getCurrentProgram();
    
    boolean isProgramLoaded();
    
    String displayProgram();
    
    String expandProgram(int level);
    
    String expandProgramWithHistory(int level);
    
    SProgram getExpandedProgram(int level) throws SProgramException;
    
    ExecutionResult runProgram(int expansionLevel, List<Integer> inputs);
    
    List<ExecutionResult> getExecutionHistory();
    
    int getMaxExpansionLevel();
    
    void saveState(String filePath) throws StateSerializationException;
    
    void loadState(String filePath) throws StateSerializationException;
    
    SystemState getCurrentState();
    
    void restoreState(SystemState state) throws SProgramException;
    
    void startDebugSession(int expansionLevel, List<Integer> inputs) throws SProgramException;
    
    boolean stepForward() throws SProgramException;
    
    boolean canStepForward();
    
    void stopDebugSession();
    
    ExecutionResult resumeExecution() throws SProgramException;
    
    ExecutionContext getCurrentExecutionState();
    
    Map<String, Integer> getChangedVariables();
}
