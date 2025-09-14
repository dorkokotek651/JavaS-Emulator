package engine.api;

import engine.model.FunctionRegistry;
import java.util.List;

public interface SProgram {
    String getName();
    
    List<SInstruction> getInstructions();
    
    List<String> getInputVariables();
    
    List<String> getLabels();
    
    int getMaxExpansionLevel();
    
    SProgram expandToLevel(int level);
    
    FunctionRegistry getFunctionRegistry();
    
    void setFunctionRegistry(FunctionRegistry functionRegistry);
}
