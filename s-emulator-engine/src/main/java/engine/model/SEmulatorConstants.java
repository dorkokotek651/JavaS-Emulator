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
    
    public static final int ZERO_VARIABLE_CYCLES = 1;
    public static final int GOTO_LABEL_CYCLES = 1;
    public static final int ASSIGNMENT_CYCLES = 4;
    public static final int CONSTANT_ASSIGNMENT_CYCLES = 2;
    public static final int JUMP_ZERO_CYCLES = 2;
    public static final int JUMP_EQUAL_CONSTANT_CYCLES = 2;
    public static final int JUMP_EQUAL_VARIABLE_CYCLES = 2;
    
    public static final Pattern X_VARIABLE_PATTERN = Pattern.compile("^x\\d+$");
    public static final Pattern Z_VARIABLE_PATTERN = Pattern.compile("^z\\d+$");
    public static final Pattern Y_VARIABLE_PATTERN = Pattern.compile("^y$");
    public static final Pattern LABEL_PATTERN = Pattern.compile("^L\\d+$|^EXIT$");
    
    public static final String RESULT_VARIABLE = "y";
}
