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
        super("JUMP_EQUAL_VARIABLE", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_VARIABLE_CYCLES);
        
        if (arguments == null || !arguments.containsKey("JEVariableLabel") || !arguments.containsKey("variableName")) {
            throw new IllegalArgumentException("JUMP_EQUAL_VARIABLE instruction requires 'JEVariableLabel' and 'variableName' arguments");
        }
        
        this.jumpLabel = arguments.get("JEVariableLabel");
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEVariableLabel cannot be null or empty");
        }
        
        this.comparedVariable = arguments.get("variableName");
        if (comparedVariable == null || comparedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("variableName cannot be null or empty");
        }
    }

    public JumpEqualVariableInstruction(String variable, String label, Map<String, String> arguments,
                                      SInstruction sourceInstruction) {
        super("JUMP_EQUAL_VARIABLE", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_VARIABLE_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey("JEVariableLabel") || !arguments.containsKey("variableName")) {
            throw new IllegalArgumentException("JUMP_EQUAL_VARIABLE instruction requires 'JEVariableLabel' and 'variableName' arguments");
        }
        
        this.jumpLabel = arguments.get("JEVariableLabel");
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEVariableLabel cannot be null or empty");
        }
        
        this.comparedVariable = arguments.get("variableName");
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
            workingVariable1, null, Map.of("assignedVariable", variable)
        );
        expandedInstructions.add(copyV);
        
        AssignmentInstruction copyVPrime = new AssignmentInstruction(
            workingVariable2, null, Map.of("assignedVariable", comparedVariable)
        );
        expandedInstructions.add(copyVPrime);
        
        JumpZeroInstruction checkZ1Zero = new JumpZeroInstruction(
            workingVariable1, compareLoopLabel, Map.of("JZLabel", checkSecondLabel)
        );
        expandedInstructions.add(checkZ1Zero);
        
        JumpZeroInstruction checkZ2Zero = new JumpZeroInstruction(
            workingVariable2, null, Map.of("JZLabel", skipLabel)
        );
        expandedInstructions.add(checkZ2Zero);
        
        DecreaseInstruction decreaseZ1 = new DecreaseInstruction(
            workingVariable1, null, Map.of()
        );
        expandedInstructions.add(decreaseZ1);
        
        DecreaseInstruction decreaseZ2 = new DecreaseInstruction(
            workingVariable2, null, Map.of()
        );
        expandedInstructions.add(decreaseZ2);
        
        GotoLabelInstruction loopBack = new GotoLabelInstruction(
            workingVariable1, null, Map.of("gotoLabel", compareLoopLabel)
        );
        expandedInstructions.add(loopBack);
        
        JumpZeroInstruction checkBothZero = new JumpZeroInstruction(
            workingVariable2, checkSecondLabel, Map.of("JZLabel", jumpLabel)
        );
        expandedInstructions.add(checkBothZero);
        
        NeutralInstruction skipDestination = new NeutralInstruction(
            variable, skipLabel, Map.of()
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
