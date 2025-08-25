package engine.expansion;

import engine.api.SInstruction;
import engine.api.SProgram;
import engine.exception.ExpansionException;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.model.SProgramImpl;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExpansionEngine {
    
    public SProgram expandProgram(SProgram program, int targetLevel) throws ExpansionException {
        if (program == null) {
            throw new ExpansionException("Program cannot be null");
        }
        
        if (targetLevel < 0) {
            throw new ExpansionException("Target expansion level cannot be negative: " + targetLevel);
        }
        
        if (targetLevel == 0) {
            return program;
        }
        
        if (targetLevel > program.getMaxExpansionLevel()) {
            throw new ExpansionException("Target level " + targetLevel + 
                " exceeds program's maximum expansion level " + program.getMaxExpansionLevel());
        }
        
        ExpansionContext context = createExpansionContext(program);
        context.setCurrentLevel(targetLevel);
        
        return expandProgramToLevel(program, targetLevel, context);
    }

    private SProgram expandProgramToLevel(SProgram program, int targetLevel, ExpansionContext context) throws ExpansionException {
        SProgramImpl expandedProgram = new SProgramImpl(program.getName() + " (expanded to level " + targetLevel + ")");
        
        List<SInstruction> currentInstructions = new ArrayList<>(program.getInstructions());
        
        for (int currentDepth = 0; currentDepth < targetLevel; currentDepth++) {
            List<SInstruction> nextInstructions = new ArrayList<>();
            boolean hasExpansion = false;
            
            updateContextWithCurrentInstructions(context, currentInstructions);
            
            for (int instIndex = 0; instIndex < currentInstructions.size(); instIndex++) {
                SInstruction instruction = currentInstructions.get(instIndex);
                if (instruction.getType() == InstructionType.BASIC) {
                    nextInstructions.add(instruction);
                } else {
                    hasExpansion = true;
                    
                    int originalLineNumber;
                    if (currentDepth == 0) {
                        originalLineNumber = instIndex + 1;
                    } else {
                        originalLineNumber = getOriginalLineNumber(instruction, instIndex + 1);
                    }
                    context.setCurrentOriginalLineNumber(originalLineNumber);
                    
                    List<SInstruction> expandedInstructions = instruction.expand(context);
                    
                    if (instruction.getLabel() != null && !instruction.getLabel().trim().isEmpty()) {
                        nextInstructions.add(new engine.model.instruction.basic.NeutralInstruction(
                            instruction.getVariable(),
                            instruction.getLabel(),
                            java.util.Map.of()
                        ));
                    }
                    
                    nextInstructions.addAll(expandedInstructions);
                }
            }
            
            currentInstructions = nextInstructions;
            
            if (!hasExpansion) {
                break;
            }
        }
        
        for (SInstruction instruction : currentInstructions) {
            expandedProgram.addInstruction(instruction);
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
    
    private void updateContextWithCurrentInstructions(ExpansionContext context, List<SInstruction> currentInstructions) {
        for (SInstruction instruction : currentInstructions) {
            if (instruction.getLabel() != null && !instruction.getLabel().trim().isEmpty()) {
            }
            
            for (String argValue : instruction.getArguments().values()) {
                if (argValue != null && !argValue.trim().isEmpty()) {
                    if (SEmulatorConstants.LABEL_PATTERN.matcher(argValue).matches() || argValue.equals(SEmulatorConstants.EXIT_LABEL)) {
                    }
                }
            }
        }
    }

    public boolean canExpand(SProgram program, int targetLevel) {
        if (program == null || targetLevel < 0) {
            return false;
        }
        
        return targetLevel <= program.getMaxExpansionLevel();
    }

    public List<SProgram> getExpansionHistory(SProgram program) throws ExpansionException {
        List<SProgram> history = new ArrayList<>();
        
        history.add(program);
        
        int maxLevel = program.getMaxExpansionLevel();
        for (int level = 1; level <= maxLevel; level++) {
            SProgram expandedProgram = expandProgram(program, level);
            history.add(expandedProgram);
        }
        
        return history;
    }
    
    private int getOriginalLineNumber(SInstruction instruction, int currentIndex) {
        if (instruction instanceof engine.model.instruction.BaseInstruction) {
            engine.model.instruction.BaseInstruction base = (engine.model.instruction.BaseInstruction) instruction;
            if (base.getOriginalLineNumber() != -1) {
                return base.getOriginalLineNumber();
            }
        }
        return currentIndex;
    }
}
