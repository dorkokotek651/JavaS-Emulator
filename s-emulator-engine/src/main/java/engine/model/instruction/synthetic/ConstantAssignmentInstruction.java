package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.instruction.synthetic.ZeroVariableInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstantAssignmentInstruction extends BaseInstruction {
    private final int constantValue;
    
    public ConstantAssignmentInstruction(String variable, String label, Map<String, String> arguments) {
        super("CONSTANT_ASSIGNMENT", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.CONSTANT_ASSIGNMENT_CYCLES);
        
        if (arguments == null || !arguments.containsKey("constantValue")) {
            throw new IllegalArgumentException("CONSTANT_ASSIGNMENT instruction requires 'constantValue' argument");
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

    public ConstantAssignmentInstruction(String variable, String label, Map<String, String> arguments,
                                       int expansionLevel, SInstruction sourceInstruction) {
        super("CONSTANT_ASSIGNMENT", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.CONSTANT_ASSIGNMENT_CYCLES, expansionLevel, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey("constantValue")) {
            throw new IllegalArgumentException("CONSTANT_ASSIGNMENT instruction requires 'constantValue' argument");
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
        context.getVariableManager().setValue(variable, constantValue);
        context.addCycles(cycles);
        context.incrementInstructionPointer();
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + constantValue;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        // V ← K expansion pattern using ZERO_VARIABLE:
        // 1. Use ZERO_VARIABLE to zero the target variable
        // 2. Increment V exactly K times (basic instructions)
        
        // Step 1: Zero the target variable using ZERO_VARIABLE
        ZeroVariableInstruction zeroInstruction = new ZeroVariableInstruction(
            variable, null, Map.of()
        );
        expandedInstructions.add(zeroInstruction);
        
        // Step 2: Increment V exactly K times
        for (int i = 0; i < constantValue; i++) {
            IncreaseInstruction increaseInstruction = new IncreaseInstruction(
                variable,
                null,
                Map.of()
            );
            expandedInstructions.add(increaseInstruction);
        }
        
        return expandedInstructions;
    }

    public int getConstantValue() {
        return constantValue;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        return 2;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        List<SInstruction> dependencies = new ArrayList<>();
        
        // Step 1: Zero variable using ZERO_VARIABLE (Level 1 dependency)
        dependencies.add(new ZeroVariableInstruction(variable, null, Map.of()));
        
        // Step 2: Increment K times (basic instructions)
        for (int i = 0; i < constantValue; i++) {
            dependencies.add(new IncreaseInstruction(variable, null, Map.of()));
        }
        
        return dependencies;
    }
}
