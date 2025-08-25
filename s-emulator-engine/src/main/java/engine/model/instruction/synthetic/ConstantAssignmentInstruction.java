package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConstantAssignmentInstruction extends BaseInstruction {
    private final int constantValue;
    
    public ConstantAssignmentInstruction(String variable, String label, Map<String, String> arguments, 
                                       SInstruction sourceInstruction) {
        super(SEmulatorConstants.CONSTANT_ASSIGNMENT_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.CONSTANT_ASSIGNMENT_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.CONSTANT_VALUE_ARG)) {
            throw new IllegalArgumentException("CONSTANT_ASSIGNMENT instruction requires 'constantValue' argument");
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
        context.getVariableManager().setValue(variable, constantValue);
        context.addCycles(cycles);
    }

    @Override
    public String getDisplayFormat() {
        return variable + " <- " + constantValue;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        ZeroVariableInstruction zeroInstruction = new ZeroVariableInstruction(
            variable, null, Map.of(), this
        );
        expandedInstructions.add(zeroInstruction);
        
        for (int i = 0; i < constantValue; i++) {
            IncreaseInstruction increaseInstruction = new IncreaseInstruction(
                variable,
                null,
                Map.of(),
                this
            );
            expandedInstructions.add(increaseInstruction);
        }
        
        return expandedInstructions;
    }

    public int getConstantValue() {
        return constantValue;
    }
    

}
