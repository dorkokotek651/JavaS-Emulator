package engine.model.instruction.basic;

import engine.model.instruction.BaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.execution.ExecutionContext;
import java.util.Map;

public class IncreaseInstruction extends BaseInstruction {
    
    public IncreaseInstruction(String variable, String label, Map<String, String> arguments) {
        super("INCREASE", InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.INCREASE_CYCLES);
    }

    @Override
    public void execute(ExecutionContext context) {
        context.getVariableManager().increment(variable);
        context.addCycles(cycles);
        context.incrementInstructionPointer();
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + variable + " + 1";
    }
}
