package engine.model.instruction;

import engine.api.SInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.instruction.basic.DecreaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.instruction.basic.NeutralInstruction;
import engine.model.instruction.synthetic.ZeroVariableInstruction;
import engine.model.instruction.synthetic.GotoLabelInstruction;
import engine.model.instruction.synthetic.AssignmentInstruction;
import engine.model.instruction.synthetic.ConstantAssignmentInstruction;
import engine.model.instruction.synthetic.JumpZeroInstruction;
import engine.model.instruction.synthetic.JumpEqualConstantInstruction;
import engine.model.instruction.synthetic.JumpEqualVariableInstruction;
import engine.model.instruction.synthetic.QuoteInstruction;
import engine.model.instruction.synthetic.JumpEqualFunctionInstruction;
import engine.model.SEmulatorConstants;
import java.util.Map;

public final class InstructionFactory {
    
    private InstructionFactory() {
    }

    public static SInstruction createInstruction(String name, String variable, String label, Map<String, String> arguments) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Instruction name cannot be null or empty");
        }
        
        if (variable == null || variable.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable cannot be null or empty");
        }

        String instructionName = name.trim().toUpperCase(java.util.Locale.ENGLISH);
        
        return switch (instructionName) {
            case SEmulatorConstants.INCREASE_NAME -> new IncreaseInstruction(variable, label, arguments);
            case SEmulatorConstants.DECREASE_NAME -> new DecreaseInstruction(variable, label, arguments);
            case SEmulatorConstants.JUMP_NOT_ZERO_NAME -> new JumpNotZeroInstruction(variable, label, arguments);
            case SEmulatorConstants.NEUTRAL_NAME -> new NeutralInstruction(variable, label, arguments);
            case SEmulatorConstants.ZERO_VARIABLE_NAME -> new ZeroVariableInstruction(variable, label, arguments, null);
            case SEmulatorConstants.GOTO_LABEL_NAME -> new GotoLabelInstruction(variable, label, arguments, null);
            case SEmulatorConstants.ASSIGNMENT_NAME -> new AssignmentInstruction(variable, label, arguments, null);
            case SEmulatorConstants.CONSTANT_ASSIGNMENT_NAME -> new ConstantAssignmentInstruction(variable, label, arguments, null);
            case SEmulatorConstants.JUMP_ZERO_NAME -> new JumpZeroInstruction(variable, label, arguments, null);
            case SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME -> new JumpEqualConstantInstruction(variable, label, arguments, null);
            case SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME -> new JumpEqualVariableInstruction(variable, label, arguments, null);
            case SEmulatorConstants.QUOTE_NAME -> new QuoteInstruction(variable, label, arguments);
            case SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME -> new JumpEqualFunctionInstruction(variable, label, arguments);
            default -> throw new IllegalArgumentException("Unknown instruction type: " + instructionName + 
                ". Supported instructions: " + SEmulatorConstants.INCREASE_NAME + ", " + SEmulatorConstants.DECREASE_NAME + ", " + 
                SEmulatorConstants.JUMP_NOT_ZERO_NAME + ", " + SEmulatorConstants.NEUTRAL_NAME + ", " + SEmulatorConstants.ZERO_VARIABLE_NAME + ", " + 
                SEmulatorConstants.GOTO_LABEL_NAME + ", " + SEmulatorConstants.ASSIGNMENT_NAME + ", " + SEmulatorConstants.CONSTANT_ASSIGNMENT_NAME + ", " + 
                SEmulatorConstants.JUMP_ZERO_NAME + ", " + SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME + ", " + SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME + ", " +
                SEmulatorConstants.QUOTE_NAME + ", " + SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME);
        };
    }
}
