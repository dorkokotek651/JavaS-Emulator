package engine.model.serialization;

import java.util.List;

public class ProgramStateData {
    private String name;
    private List<InstructionData> instructions;
    
    public ProgramStateData() {
    }
    
    public ProgramStateData(String name, List<InstructionData> instructions) {
        this.name = name;
        this.instructions = instructions;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public List<InstructionData> getInstructions() {
        return instructions;
    }
    
    public void setInstructions(List<InstructionData> instructions) {
        this.instructions = instructions;
    }
}
