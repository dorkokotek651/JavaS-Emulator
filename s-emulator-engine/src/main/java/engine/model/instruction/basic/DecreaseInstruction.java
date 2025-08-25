package engine.model.instruction.basic;

import engine.model.instruction.BaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.execution.ExecutionContext;
import java.util.Map;

public class DecreaseInstruction extends BaseInstruction {
    
    public DecreaseInstruction(String variable, String label, Map<String, String> arguments) {
        super("DECREASE", InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.DECREASE_CYCLES);
    }

    @Override
    public void execute(ExecutionContext context) {
        context.getVariableManager().decrement(variable);
        context.addCycles(cycles);
        context.incrementInstructionPointer();
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + variable + " - 1";
    }
}
