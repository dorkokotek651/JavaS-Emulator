package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ZeroVariableInstruction extends BaseInstruction {
    
    public ZeroVariableInstruction(String variable, String label, Map<String, String> arguments) {
        super("ZERO_VARIABLE", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ZERO_VARIABLE_CYCLES);
    }

    public ZeroVariableInstruction(String variable, String label, Map<String, String> arguments,
                                 int expansionLevel, SInstruction sourceInstruction) {
        super("ZERO_VARIABLE", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ZERO_VARIABLE_CYCLES, expansionLevel, sourceInstruction);
    }

    @Override
    public void execute(ExecutionContext context) {
        context.getVariableManager().setValue(variable, 0);
        context.addCycles(cycles);
        context.incrementInstructionPointer();
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- 0";
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String loopLabel = context.getUniqueLabel();
        
        DecreaseInstruction decreaseInstruction = new DecreaseInstruction(
            variable, 
            loopLabel, 
            Map.of()
        );
        
        JumpNotZeroInstruction jumpInstruction = new JumpNotZeroInstruction(
            variable,
            null,
            Map.of("JNZLabel", loopLabel)
        );
        
        expandedInstructions.add(decreaseInstruction);
        expandedInstructions.add(jumpInstruction);
        
        return expandedInstructions;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        return 1;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        String loopLabel = context.getUniqueLabel();
        
        return List.of(
            new DecreaseInstruction(variable, loopLabel, Map.of()),
            new JumpNotZeroInstruction(variable, null, Map.of("JNZLabel", loopLabel))
        );
    }
}
