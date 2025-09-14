package engine.expansion;

import engine.api.SInstruction;
import engine.api.SProgram;
import engine.exception.ExpansionException;
import engine.model.InstructionType;
import engine.model.SProgramImpl;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MultiLevelExpansionEngine {
    
    public MultiLevelExpansion expandProgramToAllLevels(SProgram program) throws ExpansionException {
        MultiLevelExpansion multiLevel = new MultiLevelExpansion();
        
        multiLevel.addLevel(0, program);
        
        int maxLevel = program.getMaxExpansionLevel();
        SProgram currentProgram = program;
        
        for (int level = 1; level <= maxLevel; level++) {
            SProgram expandedProgram = expandToNextLevel(currentProgram, level, multiLevel);
            multiLevel.addLevel(level, expandedProgram);
            currentProgram = expandedProgram;
        }
        
        return multiLevel;
    }
    
    private SProgram expandToNextLevel(SProgram sourceProgram, int targetLevel, MultiLevelExpansion multiLevel) throws ExpansionException {
        SProgramImpl expandedProgram = new SProgramImpl(sourceProgram.getName());
        
        // Copy the function registry to the expanded program
        expandedProgram.setFunctionRegistry(sourceProgram.getFunctionRegistry());
        
        ExpansionContext context = createExpansionContext(sourceProgram);
        context.setCurrentLevel(targetLevel);
        context.setFunctionRegistry(sourceProgram.getFunctionRegistry());
        
        List<SInstruction> sourceInstructions = sourceProgram.getInstructions();
        int expandedInstructionIndex = 0;
        
        for (int sourceIndex = 0; sourceIndex < sourceInstructions.size(); sourceIndex++) {
            SInstruction sourceInstruction = sourceInstructions.get(sourceIndex);
            
            if (sourceInstruction.getType() == InstructionType.BASIC) {
                expandedProgram.addInstruction(sourceInstruction);
                multiLevel.addInstructionMapping(targetLevel - 1, sourceIndex, targetLevel, expandedInstructionIndex, sourceInstruction);
                expandedInstructionIndex++;
            } else {
                List<SInstruction> expandedInstructions = sourceInstruction.expand(context);
                
                if (sourceInstruction.getLabel() != null && !sourceInstruction.getLabel().trim().isEmpty()) {
                    SInstruction neutralInstruction = new engine.model.instruction.basic.NeutralInstruction(
                        sourceInstruction.getVariable(),
                        sourceInstruction.getLabel(),
                        java.util.Map.of()
                    );
                    expandedProgram.addInstruction(neutralInstruction);
                    multiLevel.addInstructionMapping(targetLevel - 1, sourceIndex, targetLevel, expandedInstructionIndex, sourceInstruction);
                    expandedInstructionIndex++;
                }
                
                for (SInstruction expandedInstruction : expandedInstructions) {
                    expandedProgram.addInstruction(expandedInstruction);
                    multiLevel.addInstructionMapping(targetLevel - 1, sourceIndex, targetLevel, expandedInstructionIndex, sourceInstruction);
                    expandedInstructionIndex++;
                }
            }
        }
        
        return expandedProgram;
    }
    
    private ExpansionContext createExpansionContext(SProgram program) {
        Set<String> existingLabels = new HashSet<>(program.getLabels());
        Set<String> existingVariables = new HashSet<>();
        
        for (SInstruction instruction : program.getInstructions()) {
            existingVariables.add(instruction.getVariable());
            existingVariables.addAll(instruction.getArguments().values());
        }
        
        existingVariables.addAll(program.getInputVariables());
        
        return new ExpansionContext(existingLabels, existingVariables);
    }
}
