package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.instruction.basic.NeutralInstruction;
import engine.model.instruction.synthetic.GotoLabelInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class JumpZeroInstruction extends BaseInstruction {
    private final String jumpLabel;
    
    public JumpZeroInstruction(String variable, String label, Map<String, String> arguments) {
        super("JUMP_ZERO", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_ZERO_CYCLES);
        
        if (arguments == null || !arguments.containsKey("JZLabel")) {
            throw new IllegalArgumentException("JUMP_ZERO instruction requires 'JZLabel' argument");
        }
        
        this.jumpLabel = arguments.get("JZLabel");
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JZLabel cannot be null or empty");
        }
    }

    public JumpZeroInstruction(String variable, String label, Map<String, String> arguments,
                             int expansionLevel, SInstruction sourceInstruction) {
        super("JUMP_ZERO", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_ZERO_CYCLES, expansionLevel, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey("JZLabel")) {
            throw new IllegalArgumentException("JUMP_ZERO instruction requires 'JZLabel' argument");
        }
        
        this.jumpLabel = arguments.get("JZLabel");
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JZLabel cannot be null or empty");
        }
    }

    @Override
    public void execute(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        if (variableValue == 0) {
            context.jumpToLabel(jumpLabel);
        } else {
            context.incrementInstructionPointer();
        }
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " = 0 GOTO " + jumpLabel;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        // IF V = 0 GOTO L expansion pattern:
        // 1. Copy V to a working variable z (this zeros V)
        // 2. If z = 0, jump to target label
        // 3. Otherwise, restore V from z and continue
        
        String workingVariable = context.getUniqueWorkingVariable();
        String copyLoopLabel = context.getUniqueLabel();
        String skipJumpLabel = context.getUniqueLabel();
        
        // Step 1: Copy V to working variable z (this zeros V)
        // [copyLoopLabel] V ← V - 1
        // z ← z + 1
        // IF V ≠ 0 GOTO copyLoopLabel
        DecreaseInstruction copyDecrease = new DecreaseInstruction(
            variable,
            copyLoopLabel,
            Map.of()
        );
        
        IncreaseInstruction workingIncrease = new IncreaseInstruction(
            workingVariable,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction copyJump = new JumpNotZeroInstruction(
            variable,
            null,
            Map.of("JNZLabel", copyLoopLabel)
        );
        
        expandedInstructions.add(copyDecrease);
        expandedInstructions.add(workingIncrease);
        expandedInstructions.add(copyJump);
        
        // Step 2: Check if working variable is zero (meaning original V was zero)
        // IF z ≠ 0 GOTO skipJumpLabel (if V was not zero, skip the jump)
        JumpNotZeroInstruction checkJump = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of("JNZLabel", skipJumpLabel)
        );
        
        expandedInstructions.add(checkJump);
        
        // Step 3: If we reach here, V was 0, so use GOTO_LABEL to jump
        GotoLabelInstruction doJump = new GotoLabelInstruction(
            workingVariable,
            null,
            Map.of("gotoLabel", jumpLabel)
        );
        
        expandedInstructions.add(doJump);
        
        // Step 4: Restore V from working variable z (for the case where V ≠ 0)
        // [skipJumpLabel] z ← z - 1
        // V ← V + 1
        // IF z ≠ 0 GOTO skipJumpLabel
        DecreaseInstruction restoreDecrease = new DecreaseInstruction(
            workingVariable,
            skipJumpLabel,  // This instruction gets the skipJumpLabel
            Map.of()
        );
        
        IncreaseInstruction variableRestore = new IncreaseInstruction(
            variable,
            null,  // No label on the increase instruction
            Map.of()
        );
        
        JumpNotZeroInstruction restoreJump = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of("JNZLabel", skipJumpLabel)
        );
        
        expandedInstructions.add(restoreDecrease);
        expandedInstructions.add(variableRestore);
        expandedInstructions.add(restoreJump);
        
        return expandedInstructions;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        return 2;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        String skipLabel = context.getUniqueLabel();
        
        return List.of(
            // Check if V ≠ 0, if so skip the jump (basic instruction)
            new JumpNotZeroInstruction(variable, null, Map.of("JNZLabel", skipLabel)),
            
            // If V = 0, use GOTO_LABEL to jump (Level 1 dependency)
            new GotoLabelInstruction(variable, null, Map.of("gotoLabel", jumpLabel)),
            
            // Skip label (basic instruction)
            new NeutralInstruction(variable, skipLabel, Map.of())
        );
    }
}
