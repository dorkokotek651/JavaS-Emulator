package engine.expansion;

import engine.api.SInstruction;
import engine.api.SProgram;
import engine.exception.ExpansionException;
import engine.model.InstructionType;
import engine.model.SProgramImpl;
import engine.model.instruction.InstructionFactory;
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
        
        // Start with original instructions
        List<SInstruction> currentInstructions = new ArrayList<>(program.getInstructions());
        
        // Expand exactly targetLevel times (depth-based expansion)
        for (int currentDepth = 0; currentDepth < targetLevel; currentDepth++) {
            List<SInstruction> nextInstructions = new ArrayList<>();
            boolean hasExpansion = false;
            
            // Use the same context throughout all iterations to maintain label consistency
            // Only update it with new labels/variables from current instructions
            updateContextWithCurrentInstructions(context, currentInstructions);
            
            for (SInstruction instruction : currentInstructions) {
                if (instruction.getType() == InstructionType.BASIC) {
                    // Always keep basic instructions
                    nextInstructions.add(instruction);
                } else {
                    // Expand this synthetic instruction
                    hasExpansion = true;
                    List<SInstruction> dependencies = instruction.getDependencies(context);
                    
                    for (int i = 0; i < dependencies.size(); i++) {
                        SInstruction dependency = dependencies.get(i);
                        
                        // Transfer original instruction's label to first dependency
                        if (i == 0 && instruction.getLabel() != null && !instruction.getLabel().trim().isEmpty()) {
                            dependency = createInstructionWithLabel(dependency, instruction.getLabel());
                        }
                        
                        nextInstructions.add(dependency);
                    }
                }
            }
            
            currentInstructions = nextInstructions;
            
            // If no synthetic instructions were found, we can't expand further
            if (!hasExpansion) {
                break;
            }
        }
        
        // Add all final instructions to the program
        for (SInstruction instruction : currentInstructions) {
            expandedProgram.addInstruction(instruction);
        }
        
        return expandedProgram;
    }

    private SInstruction createInstructionWithLabel(SInstruction instruction, String label) {
        // Create a new instruction with the same properties but with the specified label
        return InstructionFactory.createInstruction(
            instruction.getName(),
            instruction.getVariable(),
            label,
            instruction.getArguments()
        );
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
    
    private ExpansionContext createFreshExpansionContext(List<SInstruction> currentInstructions) {
        Set<String> existingLabels = new HashSet<>();
        Set<String> existingVariables = new HashSet<>();
        
        // Collect all labels and variables from current instructions
        for (SInstruction instruction : currentInstructions) {
            if (instruction.getLabel() != null && !instruction.getLabel().trim().isEmpty()) {
                existingLabels.add(instruction.getLabel().trim());
            }
            existingVariables.add(instruction.getVariable());
            
            // Also collect variables from arguments (like jump targets, assigned variables, etc.)
            for (String argValue : instruction.getArguments().values()) {
                if (argValue != null && !argValue.trim().isEmpty()) {
                    existingVariables.add(argValue.trim());
                    // If the argument looks like a label, add it to existing labels too
                    if (argValue.matches("L\\d+") || argValue.equals("EXIT")) {
                        existingLabels.add(argValue.trim());
                    }
                }
            }
        }
        
        return new ExpansionContext(existingLabels, existingVariables);
    }
    
    private void updateContextWithCurrentInstructions(ExpansionContext context, List<SInstruction> currentInstructions) {
        // Add any new labels and variables from current instructions to the context
        for (SInstruction instruction : currentInstructions) {
            if (instruction.getLabel() != null && !instruction.getLabel().trim().isEmpty()) {
                // This will be handled by the context's internal tracking
            }
            
            // Add variables from arguments (like jump targets, assigned variables, etc.)
            for (String argValue : instruction.getArguments().values()) {
                if (argValue != null && !argValue.trim().isEmpty()) {
                    // If the argument looks like a label, the context should be aware of it
                    if (argValue.matches("L\\d+") || argValue.equals("EXIT")) {
                        // The context will handle label uniqueness internally
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
}
