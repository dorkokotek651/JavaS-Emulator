package engine.model.instruction.basic;

import engine.model.instruction.BaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.execution.ExecutionContext;
import java.util.Map;

public class JumpNotZeroInstruction extends BaseInstruction {
    private final String jumpLabel;
    
    public JumpNotZeroInstruction(String variable, String label, Map<String, String> arguments) {
        super("JUMP_NOT_ZERO", InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.JUMP_NOT_ZERO_CYCLES);
        
        if (arguments == null || !arguments.containsKey("JNZLabel")) {
            throw new IllegalArgumentException("JUMP_NOT_ZERO instruction requires 'JNZLabel' argument");
        }
        
        this.jumpLabel = arguments.get("JNZLabel");
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JNZLabel cannot be null or empty");
        }
    }

    @Override
    public void execute(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        if (variableValue != 0) {
            context.jumpToLabel(jumpLabel);
        } else {
            context.incrementInstructionPointer();
        }
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " != 0 GOTO " + jumpLabel;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }
}
