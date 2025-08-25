package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AssignmentInstruction extends BaseInstruction {
    private final String assignedVariable;
    
    public AssignmentInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.ASSIGNMENT_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ASSIGNMENT_CYCLES);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.ASSIGNED_VARIABLE_ARG)) {
            throw new IllegalArgumentException("ASSIGNMENT instruction requires 'assignedVariable' argument");
        }
        
        this.assignedVariable = arguments.get(SEmulatorConstants.ASSIGNED_VARIABLE_ARG);
        if (assignedVariable == null || assignedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedVariable cannot be null or empty");
        }
    }

    public AssignmentInstruction(String variable, String label, Map<String, String> arguments, 
                                SInstruction sourceInstruction) {
        super(SEmulatorConstants.ASSIGNMENT_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ASSIGNMENT_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.ASSIGNED_VARIABLE_ARG)) {
            throw new IllegalArgumentException("ASSIGNMENT instruction requires 'assignedVariable' argument");
        }
        
        this.assignedVariable = arguments.get(SEmulatorConstants.ASSIGNED_VARIABLE_ARG);
        if (assignedVariable == null || assignedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedVariable cannot be null or empty");
        }
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
        int sourceValue = context.getVariableManager().getValue(assignedVariable);
        context.getVariableManager().setValue(variable, sourceValue);
        context.addCycles(cycles);
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + assignedVariable;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String workingVariable = context.getUniqueWorkingVariable();
        String copyLoopLabel = context.getUniqueLabel();
        String restoreLoopLabel = context.getUniqueLabel();
        String endLabel = context.getUniqueLabel();
        
        ZeroVariableInstruction zeroTarget = new ZeroVariableInstruction(
            variable,
            null,
            Map.of(),
            this
        );
        expandedInstructions.add(zeroTarget);
        
        JumpNotZeroInstruction checkSource = new JumpNotZeroInstruction(
            assignedVariable,
            null,
            Map.of(SEmulatorConstants.JNZ_LABEL_ARG, copyLoopLabel),
            this
        );
        expandedInstructions.add(checkSource);
        
        GotoLabelInstruction skipToEnd = new GotoLabelInstruction(
            workingVariable,
            null,
            Map.of(SEmulatorConstants.GOTO_LABEL_ARG, endLabel),
            this
        );
        expandedInstructions.add(skipToEnd);
        
        DecreaseInstruction copyDecrease = new DecreaseInstruction(
            assignedVariable,
            copyLoopLabel,
            Map.of(),
            this
        );
        
        IncreaseInstruction workingIncrease = new IncreaseInstruction(
            workingVariable,
            null,
            Map.of(),
            this
        );
        
        JumpNotZeroInstruction copyJump = new JumpNotZeroInstruction(
            assignedVariable,
            null,
            Map.of(SEmulatorConstants.JNZ_LABEL_ARG, copyLoopLabel),
            this
        );
        
        expandedInstructions.add(copyDecrease);
        expandedInstructions.add(workingIncrease);
        expandedInstructions.add(copyJump);
        
        DecreaseInstruction restoreDecrease = new DecreaseInstruction(
            workingVariable,
            restoreLoopLabel,
            Map.of(),
            this
        );
        
        IncreaseInstruction targetIncrease = new IncreaseInstruction(
            variable,
            null,
            Map.of(),
            this
        );
        
        IncreaseInstruction sourceRestore = new IncreaseInstruction(
            assignedVariable,
            null,
            Map.of(),
            this
        );
        
        JumpNotZeroInstruction restoreJump = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of(SEmulatorConstants.JNZ_LABEL_ARG, restoreLoopLabel),
            this
        );
        
        expandedInstructions.add(restoreDecrease);
        expandedInstructions.add(targetIncrease);
        expandedInstructions.add(sourceRestore);
        expandedInstructions.add(restoreJump);
        
        expandedInstructions.add(new engine.model.instruction.basic.NeutralInstruction(
            variable,
            endLabel,
            Map.of(),
            this
        ));
        
        return expandedInstructions;
    }

    public String getAssignedVariable() {
        return assignedVariable;
    }
    

}
