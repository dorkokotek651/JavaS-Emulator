package engine.model;

import java.util.regex.Pattern;

public final class SEmulatorConstants {
    private SEmulatorConstants() {
    }

    public static final String EXIT_LABEL = "EXIT";
    
    public static final int INCREASE_CYCLES = 1;
    public static final int DECREASE_CYCLES = 1;
    public static final int JUMP_NOT_ZERO_CYCLES = 2;
    public static final int NEUTRAL_CYCLES = 0;

    public static final String INCREASE_NAME = "INCREASE";
    public static final String DECREASE_NAME = "DECREASE";
    public static final String JUMP_NOT_ZERO_NAME = "JUMP_NOT_ZERO";
    public static final String NEUTRAL_NAME = "NEUTRAL";
    public static final String GOTO_LABEL_NAME = "GOTO_LABEL";
    public static final String ZERO_VARIABLE_NAME = "ZERO_VARIABLE";
    public static final String ASSIGNMENT_NAME = "ASSIGNMENT";
    public static final String CONSTANT_ASSIGNMENT_NAME = "CONSTANT_ASSIGNMENT";
    public static final String JUMP_ZERO_NAME = "JUMP_ZERO";
    public static final String JUMP_EQUAL_CONSTANT_NAME = "JUMP_EQUAL_CONSTANT";
    public static final String JUMP_EQUAL_VARIABLE_NAME = "JUMP_EQUAL_VARIABLE";
    public static final String QUOTE_NAME = "QUOTE";
    public static final String JUMP_EQUAL_FUNCTION_NAME = "JUMP_EQUAL_FUNCTION";
    
    public static final int ZERO_VARIABLE_CYCLES = 1;
    public static final int GOTO_LABEL_CYCLES = 1;
    public static final int ASSIGNMENT_CYCLES = 4;
    public static final int CONSTANT_ASSIGNMENT_CYCLES = 2;
    public static final int JUMP_ZERO_CYCLES = 2;
    public static final int JUMP_EQUAL_CONSTANT_CYCLES = 2;
    public static final int JUMP_EQUAL_VARIABLE_CYCLES = 2;
    public static final int QUOTE_CYCLES = 5;
    public static final int JUMP_EQUAL_FUNCTION_CYCLES = 6;
    
    public static final Pattern X_VARIABLE_PATTERN = Pattern.compile("^x\\d+$");
    public static final Pattern Z_VARIABLE_PATTERN = Pattern.compile("^z\\d+$");
    public static final Pattern Y_VARIABLE_PATTERN = Pattern.compile("^y$");
    public static final Pattern LABEL_PATTERN = Pattern.compile("^L\\d+$|^EXIT$");
    
    public static final String RESULT_VARIABLE = "y";
    
    public static final String ASSIGNED_VARIABLE_ARG = "assignedVariable";
    public static final String JNZ_LABEL_ARG = "JNZLabel";
    public static final String GOTO_LABEL_ARG = "gotoLabel";
    public static final String JZ_LABEL_ARG = "JZLabel";
    public static final String JE_CONSTANT_LABEL_ARG = "JEConstantLabel";
    public static final String JE_VARIABLE_LABEL_ARG = "JEVariableLabel";
    public static final String CONSTANT_VALUE_ARG = "constantValue";
    public static final String VARIABLE_NAME_ARG = "variableName";
    public static final String FUNCTION_NAME_ARG = "functionName";
    public static final String FUNCTION_ARGUMENTS_ARG = "functionArguments";
    public static final String JE_FUNCTION_LABEL_ARG = "JEFunctionLabel";
    
    public static final String INSTRUCTION_TYPE_BASIC = "basic";
    public static final String INSTRUCTION_TYPE_SYNTHETIC = "synthetic";
}
