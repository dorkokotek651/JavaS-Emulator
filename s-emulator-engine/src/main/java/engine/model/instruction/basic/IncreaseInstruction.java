package engine.model.instruction.basic;

import engine.model.instruction.BaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.execution.ExecutionContext;
import engine.expansion.ExpansionContext;
import engine.api.SInstruction;
import java.util.List;
import java.util.Map;

public class IncreaseInstruction extends BaseInstruction {
    
    public IncreaseInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.INCREASE_NAME, InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.INCREASE_CYCLES);
    }

    public IncreaseInstruction(String variable, String label, Map<String, String> arguments, SInstruction sourceInstruction) {
        super(SEmulatorConstants.INCREASE_NAME, InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.INCREASE_CYCLES, sourceInstruction);
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        context.getVariableManager().increment(variable);
        context.addCycles(cycles);
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        return List.of(this);
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + variable + " + 1";
    }
}
