package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.synthetic.AssignmentInstruction;
import engine.model.instruction.synthetic.JumpZeroInstruction;
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
        super("JUMP_EQUAL_CONSTANT", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.JUMP_EQUAL_CONSTANT_CYCLES);
        
        if (arguments == null || !arguments.containsKey("JEConstantLabel") || !arguments.containsKey("constantValue")) {
            throw new IllegalArgumentException("JUMP_EQUAL_CONSTANT instruction requires 'JEConstantLabel' and 'constantValue' arguments");
        }
        
        this.jumpLabel = arguments.get("JEConstantLabel");
        if (jumpLabel == null || jumpLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("JEConstantLabel cannot be null or empty");
        }
        
        String constantStr = arguments.get("constantValue");
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
    public void execute(ExecutionContext context) {
        context.addCycles(cycles);
        
        int variableValue = context.getVariableManager().getValue(variable);
        if (variableValue == constantValue) {
            context.jumpToLabel(jumpLabel);
        } else {
            context.incrementInstructionPointer();
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
        
        AssignmentInstruction copyV = new AssignmentInstruction(
            workingVariable, null, Map.of("assignedVariable", variable)
        );
        expandedInstructions.add(copyV);
        
        for (int i = 0; i < constantValue; i++) {
            DecreaseInstruction subtractOne = new DecreaseInstruction(
                workingVariable, null, Map.of()
            );
            expandedInstructions.add(subtractOne);
        }
        
        JumpZeroInstruction checkZero = new JumpZeroInstruction(
            workingVariable, null, Map.of("JZLabel", jumpLabel)
        );
        expandedInstructions.add(checkZero);
        
        return expandedInstructions;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        return 3;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        String workingVariable = context.getUniqueWorkingVariable();
        List<SInstruction> dependencies = new ArrayList<>();
        
        // Step 1: Copy V to working variable using ASSIGNMENT (Level 2 dependency)
        dependencies.add(new AssignmentInstruction(workingVariable, null, 
            Map.of("assignedVariable", variable)));
        
        // Step 2: Subtract K from working variable (basic instructions)
        for (int i = 0; i < constantValue; i++) {
            dependencies.add(new DecreaseInstruction(workingVariable, null, Map.of()));
        }
        
        // Step 3: Use JUMP_ZERO to check if working variable is 0 (Level 2 dependency)
        dependencies.add(new JumpZeroInstruction(workingVariable, null, 
            Map.of("JZLabel", jumpLabel)));
        
        return dependencies;
    }

    public String getJumpLabel() {
        return jumpLabel;
    }

    public int getConstantValue() {
        return constantValue;
    }
}
