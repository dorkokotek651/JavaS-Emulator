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

public class JumpEqualVariableInstruction extends BaseInstruction {
    private final String jumpLabel;
    private final String comparedVariable;
    
    public JumpEqualVariableInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_VARIABLE_CYCLES);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JE_VARIABLE_LABEL_ARG) || !arguments.containsKey(SEmulatorConstants.VARIABLE_NAME_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_VARIABLE instruction requires 'JEVariableLabel' and 'variableName' arguments");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JE_VARIABLE_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEVariableLabel cannot be null or empty");
        }
        
        this.comparedVariable = arguments.get(SEmulatorConstants.VARIABLE_NAME_ARG);
        if (comparedVariable == null || comparedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("variableName cannot be null or empty");
        }
    }

        public JumpEqualVariableInstruction(String variable, String label, Map<String, String> arguments, 
                                       SInstruction sourceInstruction) {
        super(SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_VARIABLE_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.JE_VARIABLE_LABEL_ARG) || !arguments.containsKey(SEmulatorConstants.VARIABLE_NAME_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_VARIABLE instruction requires 'JEVariableLabel' and 'variableName' arguments");
        }
        
        this.jumpLabel = arguments.get(SEmulatorConstants.JE_VARIABLE_LABEL_ARG);
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEVariableLabel cannot be null or empty");
        }
        
        this.comparedVariable = arguments.get(SEmulatorConstants.VARIABLE_NAME_ARG);
        if (comparedVariable == null || comparedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("variableName cannot be null or empty");
        }
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        int comparedValue = context.getVariableManager().getValue(comparedVariable);
        if (variableValue == comparedValue) {
            context.jumpToLabel(jumpLabel);
        }
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " = " + comparedVariable + " GOTO " + jumpLabel;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String workingVariable1 = context.getUniqueWorkingVariable();
        String workingVariable2 = context.getUniqueWorkingVariable();
        String compareLoopLabel = context.getUniqueLabel();
        String checkSecondLabel = context.getUniqueLabel();
        String skipLabel = context.getUniqueLabel();
        
        AssignmentInstruction copyV = new AssignmentInstruction(
            workingVariable1, null, Map.of(SEmulatorConstants.ASSIGNED_VARIABLE_ARG, variable), this
        );
        expandedInstructions.add(copyV);
        
        AssignmentInstruction copyVPrime = new AssignmentInstruction(
            workingVariable2, null, Map.of(SEmulatorConstants.ASSIGNED_VARIABLE_ARG, comparedVariable), this
        );
        expandedInstructions.add(copyVPrime);
        
        JumpZeroInstruction checkZ1Zero = new JumpZeroInstruction(
            workingVariable1, compareLoopLabel, Map.of(SEmulatorConstants.JZ_LABEL_ARG, checkSecondLabel), this
        );
        expandedInstructions.add(checkZ1Zero);
        
        JumpZeroInstruction checkZ2Zero = new JumpZeroInstruction(
            workingVariable2, null, Map.of(SEmulatorConstants.JZ_LABEL_ARG, skipLabel), this
        );
        expandedInstructions.add(checkZ2Zero);
        
        DecreaseInstruction decreaseZ1 = new DecreaseInstruction(
            workingVariable1, null, Map.of(), this
        );
        expandedInstructions.add(decreaseZ1);
        
        DecreaseInstruction decreaseZ2 = new DecreaseInstruction(
            workingVariable2, null, Map.of(), this
        );
        expandedInstructions.add(decreaseZ2);
        
        GotoLabelInstruction loopBack = new GotoLabelInstruction(
            workingVariable1, null, Map.of(SEmulatorConstants.GOTO_LABEL_ARG, compareLoopLabel), this
        );
        expandedInstructions.add(loopBack);
        
        JumpZeroInstruction checkBothZero = new JumpZeroInstruction(
            workingVariable2, checkSecondLabel, Map.of(SEmulatorConstants.JZ_LABEL_ARG, jumpLabel), this
        );
        expandedInstructions.add(checkBothZero);
        
        NeutralInstruction skipDestination = new NeutralInstruction(
            variable, skipLabel, Map.of(), this
        );
        expandedInstructions.add(skipDestination);
        
        return expandedInstructions;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }

    public String getComparedVariable() {
        return comparedVariable;
    }
    

}
