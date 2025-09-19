package engine.api;

import engine.exception.SProgramException;
import engine.execution.ExecutionContext;
import engine.model.FunctionRegistry;
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
    
    ExecutionResult runSpecificProgram(SProgram program, int expansionLevel, List<Integer> inputs);
    ExecutionResult runSpecificProgram(SProgram program, int expansionLevel, List<Integer> inputs, int runNumber);
    
    List<ExecutionResult> getExecutionHistory();
    
    int getMaxExpansionLevel();
    
    void startDebugSession(int expansionLevel, List<Integer> inputs) throws SProgramException;
    
    void startDebugSessionForProgram(SProgram program, int expansionLevel, List<Integer> inputs) throws SProgramException;
    
    boolean stepForward() throws SProgramException;
    
    boolean canStepForward();
    
    void stopDebugSession();
    
    ExecutionResult resumeExecution() throws SProgramException;
    
    ExecutionContext getCurrentExecutionState();
    
    Map<String, Integer> getChangedVariables();
    
    FunctionRegistry getFunctionRegistry();
    
    SProgram getFunction(String functionName);
}
