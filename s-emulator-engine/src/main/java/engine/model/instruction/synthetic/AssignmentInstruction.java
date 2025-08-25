package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.instruction.synthetic.GotoLabelInstruction;
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
                               int expansionLevel, SInstruction sourceInstruction) {
        super("ASSIGNMENT", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.ASSIGNMENT_CYCLES, expansionLevel, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey("assignedVariable")) {
            throw new IllegalArgumentException("ASSIGNMENT instruction requires 'assignedVariable' argument");
        }
        
        this.assignedVariable = arguments.get("assignedVariable");
        if (assignedVariable == null || assignedVariable.trim().isEmpty()) {
            throw new IllegalArgumentException("assignedVariable cannot be null or empty");
        }
    }

    @Override
    public void execute(ExecutionContext context) {
        int sourceValue = context.getVariableManager().getValue(assignedVariable);
        context.getVariableManager().setValue(variable, sourceValue);
        context.addCycles(cycles);
        context.incrementInstructionPointer();
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + assignedVariable;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        // V ← V' expansion pattern using GOTO_LABEL:
        // 1. Zero the target variable V (basic instructions)
        // 2. Copy V' to both V and working variable (basic instructions)  
        // 3. Use GOTO_LABEL for control flow instead of basic jumps
        
        String workingVariable = context.getUniqueWorkingVariable();
        String zeroLoopLabel = context.getUniqueLabel();
        String copyLoopLabel = context.getUniqueLabel();
        String restoreLoopLabel = context.getUniqueLabel();
        String endLabel = context.getUniqueLabel();
        
        // Step 1: Zero the target variable V
        // [zeroLoopLabel] V ← V - 1; IF V ≠ 0 GOTO zeroLoopLabel
        DecreaseInstruction zeroDecrease = new DecreaseInstruction(
            variable, 
            zeroLoopLabel, 
            Map.of()
        );
        
        JumpNotZeroInstruction zeroJump = new JumpNotZeroInstruction(
            variable,
            null,
            Map.of("JNZLabel", zeroLoopLabel)
        );
        
        expandedInstructions.add(zeroDecrease);
        expandedInstructions.add(zeroJump);
        
        // Step 2: Copy loop with basic instructions
        // [copyLoopLabel] V' ← V' - 1; V ← V + 1; z ← z + 1
        DecreaseInstruction copyDecrease = new DecreaseInstruction(
            assignedVariable,
            copyLoopLabel,
            Map.of()
        );
        
        IncreaseInstruction targetIncrease = new IncreaseInstruction(
            variable,
            null,
            Map.of()
        );
        
        IncreaseInstruction workingIncrease = new IncreaseInstruction(
            workingVariable,
            null,
            Map.of()
        );
        
        // Check if V' = 0, if so continue to restore phase
        JumpNotZeroInstruction copyJump = new JumpNotZeroInstruction(
            assignedVariable,
            null,
            Map.of("JNZLabel", copyLoopLabel)
        );
        
        expandedInstructions.add(copyDecrease);
        expandedInstructions.add(targetIncrease);
        expandedInstructions.add(workingIncrease);
        expandedInstructions.add(copyJump);
        
        // Step 3: Restore loop with GOTO_LABEL
        // [restoreLoopLabel] z ← z - 1; V' ← V' + 1
        DecreaseInstruction restoreDecrease = new DecreaseInstruction(
            workingVariable,
            restoreLoopLabel,
            Map.of()
        );
        
        IncreaseInstruction sourceRestore = new IncreaseInstruction(
            assignedVariable,
            null,
            Map.of()
        );
        
        // Use basic jump for restore loop
        JumpNotZeroInstruction restoreJump = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of("JNZLabel", restoreLoopLabel)
        );
        
        expandedInstructions.add(restoreDecrease);
        expandedInstructions.add(sourceRestore);
        expandedInstructions.add(restoreJump);
        
        // Use GOTO_LABEL to demonstrate dependency on Level 1 synthetic instruction
        GotoLabelInstruction endGoto = new GotoLabelInstruction(
            workingVariable,
            null,
            Map.of("gotoLabel", endLabel)
        );
        expandedInstructions.add(endGoto);
        
        return expandedInstructions;
    }

    public String getAssignedVariable() {
        return assignedVariable;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        // ASSIGNMENT depends on ZERO_VARIABLE (Level 1), so it's Level 2
        return 2;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        String workingVariable = context.getUniqueWorkingVariable();
        String copyLoopLabel = context.getUniqueLabel();
        String restoreLoopLabel = context.getUniqueLabel();
        
        List<SInstruction> dependencies = new ArrayList<>();
        
        // Step 1: Zero target variable using ZERO_VARIABLE (Level 1 dependency)
        dependencies.add(new ZeroVariableInstruction(variable, null, Map.of()));
        
        // Step 2: Copy loop (basic instructions)
        dependencies.add(new DecreaseInstruction(assignedVariable, copyLoopLabel, Map.of()));
        dependencies.add(new IncreaseInstruction(variable, null, Map.of()));
        dependencies.add(new IncreaseInstruction(workingVariable, null, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(assignedVariable, null, Map.of("JNZLabel", copyLoopLabel)));
        
        // Step 3: Restore loop (basic instructions)
        dependencies.add(new DecreaseInstruction(workingVariable, restoreLoopLabel, Map.of()));
        dependencies.add(new IncreaseInstruction(assignedVariable, null, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(workingVariable, null, Map.of("JNZLabel", restoreLoopLabel)));
        
        return dependencies;
    }
}
