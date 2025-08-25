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
import java.util.Map;

public class InstructionFactory {
    
    private InstructionFactory() {
        // Utility class - prevent instantiation
    }

    public static SInstruction createInstruction(String name, String variable, String label, Map<String, String> arguments) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Instruction name cannot be null or empty");
        }
        
        if (variable == null || variable.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable cannot be null or empty");
        }

        String instructionName = name.trim().toUpperCase();
        
        return switch (instructionName) {
            case "INCREASE" -> new IncreaseInstruction(variable, label, arguments);
            case "DECREASE" -> new DecreaseInstruction(variable, label, arguments);
            case "JUMP_NOT_ZERO" -> new JumpNotZeroInstruction(variable, label, arguments);
            case "NEUTRAL" -> new NeutralInstruction(variable, label, arguments);
            case "ZERO_VARIABLE" -> new ZeroVariableInstruction(variable, label, arguments);
            case "GOTO_LABEL" -> new GotoLabelInstruction(variable, label, arguments);
            case "ASSIGNMENT" -> new AssignmentInstruction(variable, label, arguments);
            case "CONSTANT_ASSIGNMENT" -> new ConstantAssignmentInstruction(variable, label, arguments);
            case "JUMP_ZERO" -> new JumpZeroInstruction(variable, label, arguments);
            case "JUMP_EQUAL_CONSTANT" -> new JumpEqualConstantInstruction(variable, label, arguments);
            case "JUMP_EQUAL_VARIABLE" -> new JumpEqualVariableInstruction(variable, label, arguments);
            default -> throw new IllegalArgumentException("Unknown instruction type: " + instructionName + 
                ". Supported instructions: INCREASE, DECREASE, JUMP_NOT_ZERO, NEUTRAL, ZERO_VARIABLE, GOTO_LABEL, " +
                "ASSIGNMENT, CONSTANT_ASSIGNMENT, JUMP_ZERO, JUMP_EQUAL_CONSTANT, JUMP_EQUAL_VARIABLE");
        };
    }

    public static boolean isBasicInstruction(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String instructionName = name.trim().toUpperCase();
        return switch (instructionName) {
            case "INCREASE", "DECREASE", "JUMP_NOT_ZERO", "NEUTRAL" -> true;
            default -> false;
        };
    }

    public static boolean isSyntheticInstruction(String name) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }
        
        String instructionName = name.trim().toUpperCase();
        return switch (instructionName) {
            case "ZERO_VARIABLE", "GOTO_LABEL", "ASSIGNMENT", "CONSTANT_ASSIGNMENT", 
                 "JUMP_ZERO", "JUMP_EQUAL_CONSTANT", "JUMP_EQUAL_VARIABLE" -> true;
            default -> false;
        };
    }

    public static boolean isValidInstructionName(String name) {
        return isBasicInstruction(name) || isSyntheticInstruction(name);
    }
}
