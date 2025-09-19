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
        if (context.isVirtualExecutionMode()) {
            executeVirtual(context);
        } else {
            throw new UnsupportedOperationException("JUMP_EQUAL_FUNCTION instruction must be expanded before execution");
        }
    }
    
    private void executeVirtual(engine.execution.ExecutionContext context) {
        try {
            if (context.getFunctionRegistry() == null) {
                throw new UnsupportedOperationException("Function registry not available for virtual execution");
            }
            
            if (context.getFunctionRegistry().isEmpty()) {
                throw new UnsupportedOperationException("Function registry is empty - no functions available");
            }
            
            String jumpLabel = arguments.get(SEmulatorConstants.JE_FUNCTION_LABEL_ARG);
            String functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
            String functionArguments = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
            
            if (jumpLabel == null) {
                throw new UnsupportedOperationException("JUMP_EQUAL_FUNCTION instruction requires JEFunctionLabel argument");
            }
            
            engine.api.SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
            if (functionProgram == null) {
                throw new UnsupportedOperationException("Function not found: " + functionName);
            }
            
            // Execute the function to get its result
            String workingVar = generateUniqueVariable();
            executeFunctionCallVirtual(functionProgram, functionArguments, workingVar, context);
            
            // Check if the variable equals the function result
            int variableValue = context.getVariableManager().getValue(variable);
            int functionResult = context.getVariableManager().getValue(workingVar);
            
            if (variableValue == functionResult) {
                // Jump to the specified label (handles EXIT specially)
                context.jumpToLabel(jumpLabel);
            }
            
            context.addCycles(getCycles());
            
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new UnsupportedOperationException("Virtual execution failed for JUMP_EQUAL_FUNCTION instruction", e);
        }
    }
    
    private void executeFunctionCallVirtual(engine.api.SProgram functionProgram, String functionArguments, 
                                          String resultVariable, engine.execution.ExecutionContext context) {
        // Parse function arguments
        List<String> functionArgs = parseFunctionArguments(functionArguments);
        
        // Create function execution context
        engine.execution.ExecutionContext functionContext = createFunctionExecutionContext(functionProgram, functionArgs, context);
        
        // Execute the function
        executeFunctionProgram(functionProgram, functionContext);
        
        // Get the result
        int result = functionContext.getVariableManager().getYValue();
        context.getVariableManager().setValue(resultVariable, result);
    }
    
    private List<String> parseFunctionArguments(String argumentsStr) {
        List<String> args = new ArrayList<>();
        
        if (argumentsStr == null || argumentsStr.trim().isEmpty()) {
            return args;
        }
        
        String trimmed = argumentsStr.trim();
        
        int start = 0;
        int parenthesesLevel = 0;
        
        for (int i = 0; i < trimmed.length(); i++) {
            char c = trimmed.charAt(i);
            
            if (c == '(') {
                parenthesesLevel++;
            } else if (c == ')') {
                parenthesesLevel--;
            } else if (c == ',' && parenthesesLevel == 0) {
                String arg = trimmed.substring(start, i).trim();
                if (!arg.isEmpty()) {
                    args.add(arg);
                }
                start = i + 1;
            }
        }
        
        String lastArg = trimmed.substring(start).trim();
        if (!lastArg.isEmpty()) {
            args.add(lastArg);
        }
        return args;
    }
    
    private engine.execution.ExecutionContext createFunctionExecutionContext(engine.api.SProgram functionProgram, 
                                                                           List<String> functionArgs, 
                                                                           engine.execution.ExecutionContext mainContext) {
        engine.execution.ExecutionContext functionContext = new engine.execution.ExecutionContext();
        
        functionContext.enableDebugMode();
        functionContext.enableVirtualExecutionMode();
        functionContext.setFunctionRegistry(mainContext.getFunctionRegistry());
        
        List<String> inputVariables = functionProgram.getInputVariables();
        for (int i = 0; i < functionArgs.size() && i < inputVariables.size(); i++) {
            String inputVar = inputVariables.get(i);
            String argValue = functionArgs.get(i);
            
            int value = mainContext.getVariableManager().getValue(argValue);
            functionContext.getVariableManager().setValue(inputVar, value);
        }
        
        Map<String, Integer> labelToIndexMap = buildLabelToIndexMap(functionProgram.getInstructions());
        functionContext.setLabelToIndexMap(labelToIndexMap);
        
        return functionContext;
    }
    
    private Map<String, Integer> buildLabelToIndexMap(List<engine.api.SInstruction> instructions) {
        Map<String, Integer> labelToIndexMap = new HashMap<>();
        
        for (int i = 0; i < instructions.size(); i++) {
            engine.api.SInstruction instruction = instructions.get(i);
            String label = instruction.getLabel();
            if (label != null && !label.trim().isEmpty()) {
                labelToIndexMap.put(label.trim(), i);
            }
        }
        
        return labelToIndexMap;
    }
    
    private String generateUniqueVariable() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return "z_temp_" + timestamp + "_" + random;
    }
    
    private void executeFunctionProgram(engine.api.SProgram functionProgram, engine.execution.ExecutionContext functionContext) {
        List<engine.api.SInstruction> instructions = functionProgram.getInstructions();
        
        while (!functionContext.isProgramTerminated() && functionContext.getCurrentInstructionIndex() < instructions.size()) {
            int currentIndex = functionContext.getCurrentInstructionIndex();
            engine.api.SInstruction currentInstruction = instructions.get(currentIndex);
            functionContext.addExecutedInstruction(currentInstruction);
            
            currentInstruction.execute(functionContext);
            
            // Handle jumps
            if (functionContext.getPendingJumpLabel() != null) {
                String jumpLabel = functionContext.getPendingJumpLabel();
                Map<String, Integer> labelMap = functionContext.getLabelToIndexMap();
                Integer targetIndex = labelMap.get(jumpLabel);
                if (targetIndex != null) {
                    functionContext.setCurrentInstructionIndex(targetIndex);
                } else {
                    functionContext.terminate("Label not found: " + jumpLabel);
                }
                functionContext.clearPendingJump();
            } else {
                functionContext.setCurrentInstructionIndex(currentIndex + 1);
            }
        }
    }
}
