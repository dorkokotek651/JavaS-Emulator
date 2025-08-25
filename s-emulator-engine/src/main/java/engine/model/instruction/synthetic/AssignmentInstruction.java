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
        super("ASSIGNMENT", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ASSIGNMENT_CYCLES);
        
        if (arguments == null || !arguments.containsKey("assignedVariable")) {
            throw new IllegalArgumentException("ASSIGNMENT instruction requires 'assignedVariable' argument");
        }
        
        this.assignedVariable = arguments.get("assignedVariable");
        if (assignedVariable == null || assignedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedVariable cannot be null or empty");
        }
    }

    public AssignmentInstruction(String variable, String label, Map<String, String> arguments,
                               SInstruction sourceInstruction) {
        super("ASSIGNMENT", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ASSIGNMENT_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey("assignedVariable")) {
            throw new IllegalArgumentException("ASSIGNMENT instruction requires 'assignedVariable' argument");
        }
        
        this.assignedVariable = arguments.get("assignedVariable");
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
            Map.of()
        );
        expandedInstructions.add(zeroTarget);
        
        JumpNotZeroInstruction checkSource = new JumpNotZeroInstruction(
            assignedVariable,
            null,
            Map.of("JNZLabel", copyLoopLabel)
        );
        expandedInstructions.add(checkSource);
        
        GotoLabelInstruction skipToEnd = new GotoLabelInstruction(
            workingVariable,
            null,
            Map.of("gotoLabel", endLabel)
        );
        expandedInstructions.add(skipToEnd);
        
        DecreaseInstruction copyDecrease = new DecreaseInstruction(
            assignedVariable,
            copyLoopLabel,
            Map.of()
        );
        
        IncreaseInstruction workingIncrease = new IncreaseInstruction(
            workingVariable,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction copyJump = new JumpNotZeroInstruction(
            assignedVariable,
            null,
            Map.of("JNZLabel", copyLoopLabel)
        );
        
        expandedInstructions.add(copyDecrease);
        expandedInstructions.add(workingIncrease);
        expandedInstructions.add(copyJump);
        
        DecreaseInstruction restoreDecrease = new DecreaseInstruction(
            workingVariable,
            restoreLoopLabel,
            Map.of()
        );
        
        IncreaseInstruction targetIncrease = new IncreaseInstruction(
            variable,
            null,
            Map.of()
        );
        
        IncreaseInstruction sourceRestore = new IncreaseInstruction(
            assignedVariable,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction restoreJump = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of("JNZLabel", restoreLoopLabel)
        );
        
        expandedInstructions.add(restoreDecrease);
        expandedInstructions.add(targetIncrease);
        expandedInstructions.add(sourceRestore);
        expandedInstructions.add(restoreJump);
        
        expandedInstructions.add(new engine.model.instruction.basic.NeutralInstruction(
            variable,
            endLabel,
            Map.of()
        ));
        
        return expandedInstructions;
    }

    public String getAssignedVariable() {
        return assignedVariable;
    }
    

}
