package engine.api;

import engine.model.InstructionType;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.List;
import java.util.Map;

public interface SInstruction {
    String getName();
    
    InstructionType getType();
    
    String getVariable();
    
    String getLabel();
    
    Map<String, String> getArguments();
    
    int getCycles();
    
    List<SInstruction> expand(ExpansionContext context);
    
    void execute(ExecutionContext context);
    
    String getDisplayFormat();
    
    SInstruction getSourceInstruction();
    
    List<SInstruction> getAncestryChain();
    
    String toStringWithHistory(int currentLineNumber);
    
    String toStringWithMultiLevelHistory(List<engine.expansion.MultiLevelExpansion.InstructionAncestor> ancestry);
}
