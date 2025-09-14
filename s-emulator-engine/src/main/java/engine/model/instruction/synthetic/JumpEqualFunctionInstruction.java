package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.expansion.ExpansionContext;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.model.instruction.BaseInstruction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementation of the JUMP_EQUAL_FUNCTION instruction (Expansion Level 4).
 * 
 * This instruction makes a jump to a label if a certain variable equals the result
 * of a function run. It combines QUOTATION with JUMP_EQUAL_VARIABLE logic.
 * 
 * Cycles: 6 as per Appendix C
 * User view: IF V = Q(x1,...) GOTO L (Example: IF X2 = ID(x2) GOTO L)
 */
public class JumpEqualFunctionInstruction extends BaseInstruction {
    
    /**
     * Creates a new JUMP_EQUAL_FUNCTION instruction.
     * 
     * @param variable the variable to compare against the function result
     * @param label the instruction label (can be null)
     * @param arguments map containing JEFunctionLabel, functionName, and functionArguments
     */
    public JumpEqualFunctionInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME, InstructionType.SYNTHETIC, 
              variable, label, arguments, SEmulatorConstants.JUMP_EQUAL_FUNCTION_CYCLES);
        
        validateArguments();
    }
    
    /**
     * Creates a new JUMP_EQUAL_FUNCTION instruction with source instruction for expansion tracking.
     */
    public JumpEqualFunctionInstruction(String variable, String label, Map<String, String> arguments, 
                                       SInstruction sourceInstruction) {
        super(SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME, InstructionType.SYNTHETIC, 
              variable, label, arguments, SEmulatorConstants.JUMP_EQUAL_FUNCTION_CYCLES, 
              sourceInstruction);
        
        validateArguments();
    }
    
    private void validateArguments() {
        if (!arguments.containsKey(SEmulatorConstants.JE_FUNCTION_LABEL_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires JEFunctionLabel argument");
        }
        
        if (!arguments.containsKey(SEmulatorConstants.FUNCTION_NAME_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires functionName argument");
        }
        
        if (!arguments.containsKey(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG)) {
            throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires functionArguments argument");
        }
    }
    
    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        // For now, return a simple placeholder expansion
        // TODO: Implement full JUMP_EQUAL_FUNCTION expansion logic
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String jumpLabel = arguments.get(SEmulatorConstants.JE_FUNCTION_LABEL_ARG);
        String functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
        String functionArguments = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
        
        if (jumpLabel == null) {
            throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires JEFunctionLabel argument");
        }
        
        // Step 1: Create a working variable to store the function result
        String workingVar = context.getUniqueWorkingVariable();
        
        // Step 2: Create a QUOTE instruction to call the function
        Map<String, String> quoteArgs = new HashMap<>();
        quoteArgs.put(SEmulatorConstants.FUNCTION_NAME_ARG, functionName);
        quoteArgs.put(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG, functionArguments);
        
        SInstruction quoteInstruction = new QuoteInstruction(workingVar, null, quoteArgs, this);
        expandedInstructions.add(quoteInstruction);
        
        // Step 3: Create a JUMP_EQUAL_VARIABLE instruction to compare the result
        Map<String, String> jumpArgs = new HashMap<>();
        jumpArgs.put(SEmulatorConstants.JE_VARIABLE_LABEL_ARG, jumpLabel);
        jumpArgs.put(SEmulatorConstants.VARIABLE_NAME_ARG, workingVar);
        
        SInstruction jumpInstruction = new JumpEqualVariableInstruction(variable, label, jumpArgs, this);
        expandedInstructions.add(jumpInstruction);
        
        return expandedInstructions;
    }
    
    @Override
    public String getDisplayFormat() {
        String jumpLabel = arguments.get(SEmulatorConstants.JE_FUNCTION_LABEL_ARG);
        String functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
        String functionArgs = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
        
        if (functionArgs == null || functionArgs.trim().isEmpty()) {
            return "IF " + variable + " = (" + functionName + ") GOTO " + jumpLabel;
        } else {
            return "IF " + variable + " = (" + functionName + "," + functionArgs + ") GOTO " + jumpLabel;
        }
    }
    
    @Override
    protected void executeInstruction(engine.execution.ExecutionContext context) {
        throw new UnsupportedOperationException("JUMP_EQUAL_FUNCTION instruction must be expanded before execution");
    }
}
