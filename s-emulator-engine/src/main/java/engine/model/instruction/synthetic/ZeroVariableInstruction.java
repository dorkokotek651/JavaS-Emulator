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
                                 SInstruction sourceInstruction) {
        super("ZERO_VARIABLE", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ZERO_VARIABLE_CYCLES, sourceInstruction);
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        context.getVariableManager().setValue(variable, 0);
        context.addCycles(cycles);
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
    

}
