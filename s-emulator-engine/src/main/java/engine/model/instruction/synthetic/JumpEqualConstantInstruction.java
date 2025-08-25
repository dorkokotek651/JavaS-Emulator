package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.basic.NeutralInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpEqualConstantInstruction extends BaseInstruction {
    private final String jumpLabel;
    private final int constantValue;
    
    public JumpEqualConstantInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_CONSTANT_CYCLES);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JE_CONSTANT_LABEL_ARG) || !arguments.containsKey(SEmulatorConstants.CONSTANT_VALUE_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_CONSTANT instruction requires 'JEConstantLabel' and 'constantValue' arguments");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JE_CONSTANT_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEConstantLabel cannot be null or empty");
        }
        
        String constantStr = arguments.get(SEmulatorConstants.CONSTANT_VALUE_ARG);
        if (constantStr == null || constantStr.trim().isEmpty()) {
            throw new IllegalArgumentException("constantValue cannot be null or empty");
        }
        
        try {
            this.constantValue = Integer.parseInt(constantStr.trim());
            if (this.constantValue < 0) {
                throw new IllegalArgumentException("constantValue cannot be negative: " + this.constantValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("constantValue must be a valid non-negative integer: " + constantStr, e);
        }
    }

        public JumpEqualConstantInstruction(String variable, String label, Map<String, String> arguments, 
                                       SInstruction sourceInstruction) {
        super(SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_CONSTANT_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JE_CONSTANT_LABEL_ARG) || !arguments.containsKey(SEmulatorConstants.CONSTANT_VALUE_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_CONSTANT instruction requires 'JEConstantLabel' and 'constantValue' arguments");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JE_CONSTANT_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEConstantLabel cannot be null or empty");
        }
        
        String constantStr = arguments.get(SEmulatorConstants.CONSTANT_VALUE_ARG);
        if (constantStr == null || constantStr.trim().isEmpty()) {
            throw new IllegalArgumentException("constantValue cannot be null or empty");
        }
        
        try {
            this.constantValue = Integer.parseInt(constantStr.trim());
            if (this.constantValue < 0) {
                throw new IllegalArgumentException("constantValue cannot be negative: " + this.constantValue);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("constantValue must be a valid non-negative integer: " + constantStr, e);
        }
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        if (variableValue == constantValue) {
            context.jumpToLabel(jumpLabel);
        }
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " = " + constantValue + " GOTO " + jumpLabel;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String workingVariable = context.getUniqueWorkingVariable();
        String skipLabel = context.getUniqueLabel();
        
        AssignmentInstruction copyV = new AssignmentInstruction(
            workingVariable, null, Map.of(SEmulatorConstants.ASSIGNED_VARIABLE_ARG, variable), this
        );
        expandedInstructions.add(copyV);
        
        JumpZeroInstruction initialCheck = new JumpZeroInstruction(
            workingVariable, null, Map.of(SEmulatorConstants.JZ_LABEL_ARG, skipLabel), this
        );
        expandedInstructions.add(initialCheck);
        
        for (int i = 0; i < constantValue; i++) {
            DecreaseInstruction decreaseOne = new DecreaseInstruction(
                workingVariable, null, Map.of(), this
            );
            expandedInstructions.add(decreaseOne);
            
            JumpZeroInstruction checkZero = new JumpZeroInstruction(
                workingVariable, null, Map.of(SEmulatorConstants.JZ_LABEL_ARG, skipLabel), this
            );
            expandedInstructions.add(checkZero);
        }
        
        GotoLabelInstruction doJump = new GotoLabelInstruction(
            workingVariable, null, Map.of(SEmulatorConstants.GOTO_LABEL_ARG, jumpLabel), this
        );
        expandedInstructions.add(doJump);
        
        NeutralInstruction skipDestination = new NeutralInstruction(
            variable, skipLabel, Map.of(), this
        );
        expandedInstructions.add(skipDestination);
        
        return expandedInstructions;
    }
    
    public String getJumpLabel() {
        return jumpLabel;
    }

    public int getConstantValue() {
        return constantValue;
    }
}
