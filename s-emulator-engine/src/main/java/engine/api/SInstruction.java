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
    
    int getExpansionLevel();
    
    List<SInstruction> expand(ExpansionContext context);
    
    /**
     * Gets the dependencies of this instruction for expansion.
     * Basic instructions return List.of(this).
     * Synthetic instructions return their dependency instructions.
     * @param context the expansion context for generating unique labels/variables
     * @return list of instructions this instruction depends on
     */
    List<SInstruction> getDependencies(ExpansionContext context);
    
    void execute(ExecutionContext context);
    
    String getDisplayFormat();
    
    SInstruction getSourceInstruction();
}
