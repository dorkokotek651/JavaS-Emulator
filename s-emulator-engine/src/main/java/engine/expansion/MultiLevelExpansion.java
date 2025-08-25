package engine.expansion;

import engine.api.SInstruction;
import engine.api.SProgram;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiLevelExpansion {
    private final List<SProgram> programLevels;
    private final Map<Integer, Map<Integer, InstructionMapping>> levelMappings;
    
    public MultiLevelExpansion() {
        this.programLevels = new ArrayList<>();
        this.levelMappings = new HashMap<>();
    }
    
    public void addLevel(int level, SProgram program) {
        while (programLevels.size() <= level) {
            programLevels.add(null);
        }
        programLevels.set(level, program);
    }
    
    public SProgram getLevel(int level) {
        if (level < 0 || level >= programLevels.size()) {
            return null;
        }
        return programLevels.get(level);
    }
    
    public void addInstructionMapping(int fromLevel, int fromIndex, int toLevel, int toIndex, SInstruction sourceInstruction) {
        levelMappings.computeIfAbsent(toLevel, k -> new HashMap<>())
                   .put(toIndex, new InstructionMapping(fromLevel, fromIndex, sourceInstruction));
    }
    
    public List<InstructionAncestor> getInstructionAncestry(int level, int instructionIndex) {
        List<InstructionAncestor> ancestry = new ArrayList<>();
        
        int currentLevel = level;
        int currentIndex = instructionIndex;
        
        while (currentLevel > 0) {
            Map<Integer, InstructionMapping> mappings = levelMappings.get(currentLevel);
            if (mappings == null || !mappings.containsKey(currentIndex)) {
                break;
            }
            
            InstructionMapping mapping = mappings.get(currentIndex);
            SProgram sourceProgram = getLevel(mapping.fromLevel);
            if (sourceProgram != null && mapping.fromIndex < sourceProgram.getInstructions().size()) {
                SInstruction sourceInstruction = sourceProgram.getInstructions().get(mapping.fromIndex);
                ancestry.add(new InstructionAncestor(mapping.fromLevel, mapping.fromIndex + 1, sourceInstruction));
            }
            
            currentLevel = mapping.fromLevel;
            currentIndex = mapping.fromIndex;
        }
        
        return ancestry;
    }
    
    public int getMaxLevel() {
        return programLevels.size() - 1;
    }
    
    public static class InstructionMapping {
        public final int fromLevel;
        public final int fromIndex;
        public final SInstruction sourceInstruction;
        
        public InstructionMapping(int fromLevel, int fromIndex, SInstruction sourceInstruction) {
            this.fromLevel = fromLevel;
            this.fromIndex = fromIndex;
            this.sourceInstruction = sourceInstruction;
        }
    }
    
    public static class InstructionAncestor {
        public final int level;
        public final int lineNumber;
        public final SInstruction instruction;
        
        public InstructionAncestor(int level, int lineNumber, SInstruction instruction) {
            this.level = level;
            this.lineNumber = lineNumber;
            this.instruction = instruction;
        }
    }
}
