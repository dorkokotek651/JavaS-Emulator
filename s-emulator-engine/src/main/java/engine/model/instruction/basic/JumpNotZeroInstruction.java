package engine.model.instruction.basic;

import engine.model.instruction.BaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.execution.ExecutionContext;
import engine.expansion.ExpansionContext;
import engine.api.SInstruction;
import java.util.List;
import java.util.Map;

public class JumpNotZeroInstruction extends BaseInstruction {
    private final String jumpLabel;
    
    public JumpNotZeroInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.JUMP_NOT_ZERO_NAME, InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.JUMP_NOT_ZERO_CYCLES);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JNZ_LABEL_ARG)) {
            throw new IllegalArgumentException("JUMP_NOT_ZERO instruction requires 'JNZLabel' argument");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JNZ_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JNZLabel cannot be null or empty");
        }
    }

    public JumpNotZeroInstruction(String variable, String label, Map<String, String> arguments, SInstruction sourceInstruction) {
        super(SEmulatorConstants.JUMP_NOT_ZERO_NAME, InstructionType.BASIC, variable, label, arguments, SEmulatorConstants.JUMP_NOT_ZERO_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JNZ_LABEL_ARG)) {
            throw new IllegalArgumentException("JUMP_NOT_ZERO instruction requires 'JNZLabel' argument");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JNZ_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JNZLabel cannot be null or empty");
        }
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        if (variableValue != 0) {
            context.jumpToLabel(jumpLabel);
        }
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        return List.of(this);
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " != 0 GOTO " + jumpLabel;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }
}
