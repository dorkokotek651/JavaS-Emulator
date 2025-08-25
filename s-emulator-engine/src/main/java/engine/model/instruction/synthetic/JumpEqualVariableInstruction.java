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
                                      int expansionLevel, SInstruction sourceInstruction) {
        super("JUMP_EQUAL_VARIABLE", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_VARIABLE_CYCLES, expansionLevel, sourceInstruction);
        
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
    public void execute(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        int comparedValue = context.getVariableManager().getValue(comparedVariable);
        if (variableValue == comparedValue) {
            context.jumpToLabel(jumpLabel);
        } else {
            context.incrementInstructionPointer();
        }
    }

    @Override
    public String getDisplayFormat() {
        return "IF " + variable + " = " + comparedVariable + " GOTO " + jumpLabel;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        // IF V = V' GOTO L expansion pattern:
        // 1. Copy V to working variable z1 (preserving V)
        // 2. Copy V' to working variable z2 (preserving V')
        // 3. Simultaneously decrement z1 and z2
        // 4. If both reach zero at the same time, V = V', so jump
        // 5. Otherwise, restore both V and V' and continue
        
        String workingVariable1 = context.getUniqueWorkingVariable(); // Copy of V
        String workingVariable2 = context.getUniqueWorkingVariable(); // Copy of V'
        
        String copyV1LoopLabel = context.getUniqueLabel();
        String copyV2LoopLabel = context.getUniqueLabel();
        String compareLoopLabel = context.getUniqueLabel();
        String checkEqualLabel = context.getUniqueLabel();
        String restoreV1LoopLabel = context.getUniqueLabel();
        String restoreV2LoopLabel = context.getUniqueLabel();
        String skipJumpLabel = context.getUniqueLabel();
        
        // Step 1: Copy V to working variable z1
        // [copyV1LoopLabel] V ← V - 1
        // z1 ← z1 + 1
        // IF V ≠ 0 GOTO copyV1LoopLabel
        DecreaseInstruction copyV1Decrease = new DecreaseInstruction(
            variable,
            copyV1LoopLabel,
            Map.of()
        );
        
        IncreaseInstruction working1Increase = new IncreaseInstruction(
            workingVariable1,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction copyV1Jump = new JumpNotZeroInstruction(
            variable,
            null,
            Map.of("JNZLabel", copyV1LoopLabel)
        );
        
        expandedInstructions.add(copyV1Decrease);
        expandedInstructions.add(working1Increase);
        expandedInstructions.add(copyV1Jump);
        
        // Step 2: Copy V' to working variable z2
        // [copyV2LoopLabel] V' ← V' - 1
        // z2 ← z2 + 1
        // IF V' ≠ 0 GOTO copyV2LoopLabel
        DecreaseInstruction copyV2Decrease = new DecreaseInstruction(
            comparedVariable,
            copyV2LoopLabel,
            Map.of()
        );
        
        IncreaseInstruction working2Increase = new IncreaseInstruction(
            workingVariable2,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction copyV2Jump = new JumpNotZeroInstruction(
            comparedVariable,
            null,
            Map.of("JNZLabel", copyV2LoopLabel)
        );
        
        expandedInstructions.add(copyV2Decrease);
        expandedInstructions.add(working2Increase);
        expandedInstructions.add(copyV2Jump);
        
        // Step 3: Simultaneously decrement z1 and z2
        // [compareLoopLabel] z1 ← z1 - 1
        // z2 ← z2 - 1
        // IF z1 ≠ 0 GOTO checkEqualLabel
        // IF z2 ≠ 0 GOTO skipJumpLabel (z1=0 but z2≠0, so V < V')
        // GOTO jumpLabel (both are 0, so V = V')
        DecreaseInstruction working1Decrease = new DecreaseInstruction(
            workingVariable1,
            compareLoopLabel,
            Map.of()
        );
        
        DecreaseInstruction working2Decrease = new DecreaseInstruction(
            workingVariable2,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction check1Jump = new JumpNotZeroInstruction(
            workingVariable1,
            null,
            Map.of("JNZLabel", checkEqualLabel)
        );
        
        JumpNotZeroInstruction check2Jump = new JumpNotZeroInstruction(
            workingVariable2,
            null,
            Map.of("JNZLabel", skipJumpLabel)
        );
        
        // Use GOTO_LABEL when both are zero (V = V')
        GotoLabelInstruction doJump = new GotoLabelInstruction(
            workingVariable1,
            null,
            Map.of("gotoLabel", jumpLabel)
        );
        
        expandedInstructions.add(working1Decrease);
        expandedInstructions.add(working2Decrease);
        expandedInstructions.add(check1Jump);
        expandedInstructions.add(check2Jump);
        expandedInstructions.add(doJump);
        
        // Step 4: Check if z2 is also zero (continue comparison)
        // [checkEqualLabel] IF z2 ≠ 0 GOTO compareLoopLabel
        // GOTO skipJumpLabel (z1≠0 but z2=0, so V > V')
        JumpNotZeroInstruction continueCompare = new JumpNotZeroInstruction(
            workingVariable2,
            checkEqualLabel,
            Map.of("JNZLabel", compareLoopLabel)
        );
        
        // Use GOTO_LABEL to skip jump (z1≠0 but z2=0, so V > V')
        GotoLabelInstruction skipJump = new GotoLabelInstruction(
            workingVariable1,
            null,
            Map.of("gotoLabel", skipJumpLabel)
        );
        
        expandedInstructions.add(continueCompare);
        expandedInstructions.add(skipJump);
        
        // Step 5: Restore V from working variable z1
        // [skipJumpLabel] [restoreV1LoopLabel] z1 ← z1 - 1
        // V ← V + 1
        // IF z1 ≠ 0 GOTO restoreV1LoopLabel
        DecreaseInstruction restoreV1Decrease = new DecreaseInstruction(
            workingVariable1,
            skipJumpLabel,
            Map.of()
        );
        
        IncreaseInstruction variableRestore = new IncreaseInstruction(
            variable,
            restoreV1LoopLabel,
            Map.of()
        );
        
        JumpNotZeroInstruction restoreV1Jump = new JumpNotZeroInstruction(
            workingVariable1,
            null,
            Map.of("JNZLabel", restoreV1LoopLabel)
        );
        
        expandedInstructions.add(restoreV1Decrease);
        expandedInstructions.add(variableRestore);
        expandedInstructions.add(restoreV1Jump);
        
        // Step 6: Restore V' from working variable z2
        // [restoreV2LoopLabel] z2 ← z2 - 1
        // V' ← V' + 1
        // IF z2 ≠ 0 GOTO restoreV2LoopLabel
        DecreaseInstruction restoreV2Decrease = new DecreaseInstruction(
            workingVariable2,
            restoreV2LoopLabel,
            Map.of()
        );
        
        IncreaseInstruction comparedRestore = new IncreaseInstruction(
            comparedVariable,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction restoreV2Jump = new JumpNotZeroInstruction(
            workingVariable2,
            null,
            Map.of("JNZLabel", restoreV2LoopLabel)
        );
        
        expandedInstructions.add(restoreV2Decrease);
        expandedInstructions.add(comparedRestore);
        expandedInstructions.add(restoreV2Jump);
        
        return expandedInstructions;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }

    public String getComparedVariable() {
        return comparedVariable;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        return 3;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        String workingVariable1 = context.getUniqueWorkingVariable();
        String workingVariable2 = context.getUniqueWorkingVariable();
        String copyV1LoopLabel = context.getUniqueLabel();
        String copyV2LoopLabel = context.getUniqueLabel();
        String compareLoopLabel = context.getUniqueLabel();
        String checkLabel = context.getUniqueLabel();
        String skipLabel = context.getUniqueLabel();
        String restoreV1LoopLabel = context.getUniqueLabel();
        String restoreV2LoopLabel = context.getUniqueLabel();
        
        List<SInstruction> dependencies = new ArrayList<>();
        
        // Step 1: Copy V to working variable z1 (basic instructions)
        dependencies.add(new DecreaseInstruction(variable, copyV1LoopLabel, Map.of()));
        dependencies.add(new IncreaseInstruction(workingVariable1, null, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(variable, null, Map.of("JNZLabel", copyV1LoopLabel)));
        
        // Step 2: Copy V' to working variable z2 (basic instructions)
        dependencies.add(new DecreaseInstruction(comparedVariable, copyV2LoopLabel, Map.of()));
        dependencies.add(new IncreaseInstruction(workingVariable2, null, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(comparedVariable, null, Map.of("JNZLabel", copyV2LoopLabel)));
        
        // Step 3: Compare loop with basic instructions
        dependencies.add(new DecreaseInstruction(workingVariable1, compareLoopLabel, Map.of()));
        dependencies.add(new DecreaseInstruction(workingVariable2, null, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(workingVariable1, null, Map.of("JNZLabel", checkLabel)));
        dependencies.add(new JumpNotZeroInstruction(workingVariable2, null, Map.of("JNZLabel", skipLabel)));
        
        // Step 4: Both are zero - use GOTO_LABEL for equal case (Level 1 dependency)
        dependencies.add(new GotoLabelInstruction(workingVariable1, null, Map.of("gotoLabel", jumpLabel)));
        
        // Step 5: Check second variable with basic instruction
        dependencies.add(new JumpNotZeroInstruction(workingVariable2, checkLabel, Map.of("JNZLabel", compareLoopLabel)));
        
        // Step 6: Use GOTO_LABEL to skip (Level 1 dependency)
        dependencies.add(new GotoLabelInstruction(workingVariable1, null, Map.of("gotoLabel", skipLabel)));
        
        // Step 7: Restore V from working variable z1 (basic instructions)
        dependencies.add(new DecreaseInstruction(workingVariable1, skipLabel, Map.of()));
        dependencies.add(new IncreaseInstruction(variable, restoreV1LoopLabel, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(workingVariable1, null, Map.of("JNZLabel", restoreV1LoopLabel)));
        
        // Step 8: Restore V' from working variable z2 (basic instructions)
        dependencies.add(new DecreaseInstruction(workingVariable2, restoreV2LoopLabel, Map.of()));
        dependencies.add(new IncreaseInstruction(comparedVariable, null, Map.of()));
        dependencies.add(new JumpNotZeroInstruction(workingVariable2, null, Map.of("JNZLabel", restoreV2LoopLabel)));
        
        return dependencies;
    }
}
