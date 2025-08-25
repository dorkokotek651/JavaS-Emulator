package engine.model.instruction.basic;

import engine.model.instruction.BaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.execution.ExecutionContext;
import java.util.Map;

public class NeutralInstruction extends BaseInstruction {
    
    public NeutralInstruction(String variable, String label, Map<String, String> arguments) {
        super("NEUTRAL", InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.NEUTRAL_CYCLES);
    }

    @Override
    public void execute(ExecutionContext context) {
        // Neutral instruction does nothing to the variable (V <- V)
        // It only consumes 0 cycles and moves to next instruction
        context.addCycles(cycles);
        context.incrementInstructionPointer();
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + variable;
    }
}
