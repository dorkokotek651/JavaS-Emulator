package engine.api;

import engine.exception.SProgramException;
import java.util.List;

public interface SEmulatorEngine {
    void loadProgram(String xmlFilePath) throws SProgramException;
    
    SProgram getCurrentProgram();
    
    boolean isProgramLoaded();
    
    String displayProgram();
    
    String expandProgram(int level);
    
    ExecutionResult runProgram(int expansionLevel, List<Integer> inputs);
    
    List<ExecutionResult> getExecutionHistory();
    
    int getMaxExpansionLevel();
}
