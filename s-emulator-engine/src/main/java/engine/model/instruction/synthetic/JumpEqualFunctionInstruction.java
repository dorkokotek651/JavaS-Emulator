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

public class JumpEqualFunctionInstruction extends BaseInstruction {
    
    public JumpEqualFunctionInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME, InstructionType.SYNTHETIC, 
              variable, label, arguments, SEmulatorConstants.JUMP_EQUAL_FUNCTION_CYCLES);
        
        validateArguments();
    }
    
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
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String jumpLabel = arguments.get(SEmulatorConstants.JE_FUNCTION_LABEL_ARG);
        String functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
        String functionArguments = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
        
        if (jumpLabel == null) {
            throw new IllegalArgumentException("JUMP_EQUAL_FUNCTION instruction requires JEFunctionLabel argument");
        }
        
        String workingVar = context.getUniqueWorkingVariable();
        
        Map<String, String> quoteArgs = new HashMap<>();
        quoteArgs.put(SEmulatorConstants.FUNCTION_NAME_ARG, functionName);
        quoteArgs.put(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG, functionArguments);
        
        SInstruction quoteInstruction = new QuoteInstruction(workingVar, null, quoteArgs, this);
        expandedInstructions.add(quoteInstruction);
        
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
