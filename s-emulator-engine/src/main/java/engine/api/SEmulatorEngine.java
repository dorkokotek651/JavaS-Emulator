package engine.api;

import engine.exception.SProgramException;
import engine.exception.StateSerializationException;
import java.util.List;

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
}
