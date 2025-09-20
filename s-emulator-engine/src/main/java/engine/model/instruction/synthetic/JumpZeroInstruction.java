package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.instruction.basic.NeutralInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZeroInstruction extends BaseInstruction {
    private final String jumpLabel;
    
    public JumpZeroInstruction(String variable, String label, Map<String, String> arguments, 
                             SInstruction sourceInstruction) {
        super(SEmulatorConstants.JUMP_ZERO_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_ZERO_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JZ_LABEL_ARG)) {
            throw new IllegalArgumentException("JUMP_ZERO instruction requires 'JZLabel' argument");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JZ_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JZLabel cannot be null or empty");
        }
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        if (variableValue == 0) {
            context.jumpToLabel(jumpLabel);
        }
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " = 0 GOTO " + jumpLabel;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String skipLabel = context.getUniqueLabel();
        String workingVariable = context.getUniqueWorkingVariable();
        
        JumpNotZeroInstruction skipJump = new JumpNotZeroInstruction(
            variable,
            null,
            Map.of(SEmulatorConstants.JNZ_LABEL_ARG, skipLabel),
            this
        );
        expandedInstructions.add(skipJump);
        
        GotoLabelInstruction doJump = new GotoLabelInstruction(
            workingVariable,
            null,
            Map.of(SEmulatorConstants.GOTO_LABEL_ARG, jumpLabel),
            this
        );
        expandedInstructions.add(doJump);
        
        NeutralInstruction skipDestination = new NeutralInstruction(
            variable,
            skipLabel,
            Map.of(),
            this
        );
        expandedInstructions.add(skipDestination);
        
        return expandedInstructions;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }

}
