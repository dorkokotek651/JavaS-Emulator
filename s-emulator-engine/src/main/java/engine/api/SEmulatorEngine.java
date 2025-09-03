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
    
    ExecutionResult runProgram(int expansionLevel, List<Integer> inputs);
    
    List<ExecutionResult> getExecutionHistory();
    
    int getMaxExpansionLevel();
    
    void saveState(String filePath) throws StateSerializationException;
    
    void loadState(String filePath) throws StateSerializationException;
    
    SystemState getCurrentState();
    
    void restoreState(SystemState state) throws SProgramException;
    
    // Debug session methods
    /**
     * Starts a debug session for step-by-step program execution.
     * 
     * @param expansionLevel the expansion level to use for execution
     * @param inputs the input values for the program
     * @throws SProgramException if no program is loaded or debug session cannot be started
     */
    void startDebugSession(int expansionLevel, List<Integer> inputs) throws SProgramException;
    
    /**
     * Executes a single instruction in debug mode.
     * 
     * @return true if instruction was executed, false if program has ended
     * @throws SProgramException if no debug session is active
     */
    boolean stepForward() throws SProgramException;
    
    /**
     * Checks if more instructions can be executed in debug mode.
     * 
     * @return true if more steps are available, false if program has ended
     */
    boolean canStepForward();
    
    /**
     * Stops the current debug session and cleans up resources.
     */
    void stopDebugSession();
    
    /**
     * Resumes execution from current debug state to completion.
     * 
     * @return the execution result after completion
     * @throws SProgramException if no debug session is active
     */
    ExecutionResult resumeExecution() throws SProgramException;
    
    /**
     * Gets the current execution context during debug session.
     * 
     * @return the current execution context, or null if no debug session is active
     */
    ExecutionContext getCurrentExecutionState();
    
    /**
     * Gets variables that changed in the last debug step.
     * 
     * @return map of variable names to their new values that changed in last step
     */
    Map<String, Integer> getChangedVariables();
}
