package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.api.SProgram;
import engine.expansion.ExpansionContext;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.InstructionFactory;
import engine.util.CompositionParser;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuoteInstruction extends BaseInstruction {
    
    private final String functionName;
    private final String functionArguments;
    
    public QuoteInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.QUOTE_NAME, InstructionType.SYNTHETIC, 
              variable, label, arguments, SEmulatorConstants.QUOTE_CYCLES);
        
        validateArguments();
        this.functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
        this.functionArguments = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
    }
    
    public QuoteInstruction(String variable, String label, Map<String, String> arguments, 
                           SInstruction sourceInstruction) {
        super(SEmulatorConstants.QUOTE_NAME, InstructionType.SYNTHETIC, 
              variable, label, arguments, SEmulatorConstants.QUOTE_CYCLES, sourceInstruction);
        
        validateArguments();
        this.functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
        this.functionArguments = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
    }
    
    private void validateArguments() {
        if (!arguments.containsKey(SEmulatorConstants.FUNCTION_NAME_ARG)) {
            throw new IllegalArgumentException("QUOTE instruction requires functionName argument");
        }
        
        if (!arguments.containsKey(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG)) {
            throw new IllegalArgumentException("QUOTE instruction requires functionArguments argument");
        }
    }
    
    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        if (context.getFunctionRegistry() == null) {
            throw new IllegalArgumentException("Function registry not available in expansion context");
        }

        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        List<String> functionArgs = parseFunctionArguments(functionArguments);
        
        List<String> resolvedArgs = resolveArguments(functionArgs, expandedInstructions, context);
        
        expandSingleFunction(functionName, resolvedArgs, variable, expandedInstructions, context);
        
        return expandedInstructions;
    }
    
    private List<String> resolveArguments(List<String> rawArgs, List<SInstruction> expandedInstructions, 
                                        ExpansionContext context) {
        List<String> resolvedArgs = new ArrayList<>();
        
        for (String arg : rawArgs) {
            if (arg.startsWith("(") && arg.endsWith(")")) {
                String resultVar = expandComposition(arg, expandedInstructions, context);
                resolvedArgs.add(resultVar);
            } else {
                resolvedArgs.add(arg);
            }
        }
        return resolvedArgs;
    }
    
    private String expandComposition(String composition, List<SInstruction> expandedInstructions, 
                                   ExpansionContext context) {
        try {
            List<CompositionParser.FunctionCall> functionCalls = CompositionParser.parseComposition(composition);
            
            if (functionCalls.isEmpty()) {
                throw new IllegalArgumentException("Empty function composition: " + composition);
            }
            
            CompositionParser.FunctionCall functionCall = functionCalls.get(0);
            
            String resultVar = context.getUniqueWorkingVariable();
            context.markVariableAsUsed(resultVar);
            
            expandSingleFunction(functionCall.getFunctionName(), functionCall.getArguments(), 
                               resultVar, expandedInstructions, context);
            
            return resultVar;
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to expand function composition: " + composition, e);
        }
    }
    
    private void expandSingleFunction(String functionName, List<String> args, String resultVar, 
                                    List<SInstruction> expandedInstructions, ExpansionContext context) {

        SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
        if (functionProgram == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        
        Map<String, String> variableMapping = createVariableMapping(args, context);
        
        Map<String, String> labelMapping = createLabelMapping(functionProgram, context);
        String endLabel = context.getUniqueLabel();
        context.markLabelAsUsed(endLabel);
        
        createInputAssignments(args, variableMapping, expandedInstructions);
        
        addSubstitutedInstructions(functionProgram, variableMapping, labelMapping, endLabel, expandedInstructions);
        
        createResultAssignment(variableMapping.get("y"), resultVar, endLabel, expandedInstructions);
    }
    
    private Map<String, String> createVariableMapping(List<String> args, ExpansionContext context) {
        Map<String, String> mapping = new HashMap<>();
        
        for (int i = 0; i < args.size(); i++) {
            String inputVar = "x" + (i + 1);
            String uniqueVar = context.getUniqueWorkingVariable();
            mapping.put(inputVar, uniqueVar);
            context.markVariableAsUsed(uniqueVar);
        }
        
        String yVar = context.getUniqueWorkingVariable();
        mapping.put("y", yVar);
        context.markVariableAsUsed(yVar);
        
        return mapping;
    }
    
    private Map<String, String> createLabelMapping(SProgram functionProgram, ExpansionContext context) {
        Map<String, String> labelMapping = new HashMap<>();
        
        for (String label : functionProgram.getLabels()) {
            if (!label.equals(SEmulatorConstants.EXIT_LABEL)) {
                String uniqueLabel = context.getUniqueLabel();
                labelMapping.put(label, uniqueLabel);
                context.markLabelAsUsed(uniqueLabel);
            }
        }
        
        return labelMapping;
    }
    
    private void createInputAssignments(List<String> args, Map<String, String> variableMapping, 
                                      List<SInstruction> expandedInstructions) {
        for (int i = 0; i < args.size(); i++) {
            String inputVar = "x" + (i + 1);
            String uniqueVar = variableMapping.get(inputVar);
            String sourceVar = args.get(i);
            
            Map<String, String> assignmentArgs = new HashMap<>();
            assignmentArgs.put(SEmulatorConstants.ASSIGNED_VARIABLE_ARG, sourceVar);
            
            SInstruction assignment = new AssignmentInstruction(uniqueVar, null, assignmentArgs, this);
            expandedInstructions.add(assignment);
        }
    }
    
    private void addSubstitutedInstructions(SProgram functionProgram, Map<String, String> variableMapping,
                                          Map<String, String> labelMapping, String endLabel,
                                          List<SInstruction> expandedInstructions) {
        
        for (SInstruction instruction : functionProgram.getInstructions()) {
            SInstruction substituted = substituteInstruction(instruction, variableMapping, labelMapping, endLabel);
            expandedInstructions.add(substituted);
        }
    }
    
    private void createResultAssignment(String yVar, String resultVar, String endLabel,
                                      List<SInstruction> expandedInstructions) {
        Map<String, String> assignmentArgs = new HashMap<>();
        assignmentArgs.put(SEmulatorConstants.ASSIGNED_VARIABLE_ARG, yVar);
        
        SInstruction resultAssignment = new AssignmentInstruction(resultVar, endLabel, assignmentArgs, this);
        expandedInstructions.add(resultAssignment);
    }
    
    private SInstruction substituteInstruction(SInstruction instruction, Map<String, String> variableMapping,
                                             Map<String, String> labelMapping, String endLabel) {
        try {
            String newVariable = substituteVariable(instruction.getVariable(), variableMapping);
            
            String newLabel = substituteLabel(instruction.getLabel(), labelMapping);
            
            Map<String, String> newArguments = substituteArguments(instruction.getArguments(), 
                                                                  variableMapping, labelMapping, endLabel);
            
            return InstructionFactory.createInstruction(
                instruction.getName(), newVariable, newLabel, newArguments);
                
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to substitute instruction: " + 
                instruction.getDisplayFormat(), e);
        }
    }
    
    private String substituteVariable(String variable, Map<String, String> variableMapping) {
        if (variable == null) {
            return null;
        }
        return variableMapping.getOrDefault(variable, variable);
    }
    
    private String substituteLabel(String label, Map<String, String> labelMapping) {
        if (label == null) {
            return null;
        }
        return labelMapping.getOrDefault(label, label);
    }
    
    private Map<String, String> substituteArguments(Map<String, String> arguments, 
                                                   Map<String, String> variableMapping,
                                                   Map<String, String> labelMapping, 
                                                   String endLabel) {
        Map<String, String> newArguments = new HashMap<>();
        
        for (Map.Entry<String, String> entry : arguments.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String newValue = value;
            
            if (key.equals(SEmulatorConstants.ASSIGNED_VARIABLE_ARG) || 
                key.equals(SEmulatorConstants.VARIABLE_NAME_ARG)) {
                if (variableMapping.containsKey(value)) {
                    newValue = substituteVariable(value, variableMapping);
                }
            }
            else if (key.equals(SEmulatorConstants.JNZ_LABEL_ARG) || 
                     key.equals(SEmulatorConstants.JZ_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.GOTO_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.JE_CONSTANT_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.JE_VARIABLE_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.JE_FUNCTION_LABEL_ARG)) {
                
                if (SEmulatorConstants.EXIT_LABEL.equals(value)) {
                    newValue = endLabel;
                } else {
                    newValue = substituteLabel(value, labelMapping);
                }
            }
            
            newArguments.put(key, newValue);
        }
        
        return newArguments;
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
    
    @Override
    public String getDisplayFormat() {
        if (functionArguments == null || functionArguments.trim().isEmpty()) {
            return variable + " ← (" + functionName + ")";
        } else {
            return variable + " ← (" + functionName + "," + functionArguments + ")";
        }
    }
    
    @Override
    protected void executeInstruction(engine.execution.ExecutionContext context) {
        if (context.isVirtualExecutionMode()) {
            executeVirtual(context);
        } else {
            throw new UnsupportedOperationException("QUOTE instruction must be expanded before execution");
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
            
            engine.api.SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
            if (functionProgram == null) {
                throw new UnsupportedOperationException("Function not found: " + functionName);
            }
            
            List<String> functionArgs = parseFunctionArguments(functionArguments);
            
            List<String> resolvedArgs = resolveArgumentsForVirtualExecution(functionArgs, context);
            
            executeFunctionCallVirtual(functionProgram, resolvedArgs, getVariable(), context);
            
            context.addCycles(getCycles());
            
        } catch (UnsupportedOperationException e) {
            throw e;
        } catch (Exception e) {
            throw new UnsupportedOperationException("Virtual execution failed for QUOTE instruction", e);
        }
    }
    
    private List<String> resolveArgumentsForVirtualExecution(List<String> rawArgs, 
                                                           engine.execution.ExecutionContext context) {
        List<String> resolvedArgs = new ArrayList<>();
        
        for (String arg : rawArgs) {
            if (arg.startsWith("(") && arg.endsWith(")")) {
                String resultVar = executeCompositionVirtual(arg, context);
                resolvedArgs.add(resultVar);
            } else {
                resolvedArgs.add(arg);
            }
        }
        return resolvedArgs;
    }
    
    private String executeCompositionVirtual(String composition, engine.execution.ExecutionContext context) {
        try {
            List<CompositionParser.FunctionCall> functionCalls = CompositionParser.parseComposition(composition);
            
            if (functionCalls.isEmpty()) {
                throw new IllegalArgumentException("Empty function composition: " + composition);
            }
            
            if (functionCalls.size() == 1) {
                CompositionParser.FunctionCall functionCall = functionCalls.get(0);
                
                String resultVar = generateUniqueVariable();
                
                List<String> processedArgs = new ArrayList<>();
                for (String arg : functionCall.getArguments()) {
                    if (arg.startsWith("(") && arg.endsWith(")")) {
                        String nestedResult = executeCompositionVirtual(arg, context);
                        processedArgs.add(nestedResult);
                    } else {
                        processedArgs.add(arg);
                    }
                }
                
                executeSingleFunctionVirtual(functionCall.getFunctionName(), processedArgs, resultVar, context);
                
                return resultVar;
            }
            
            String currentResultVar = null;
            
            for (int i = 0; i < functionCalls.size(); i++) {
                CompositionParser.FunctionCall functionCall = functionCalls.get(i);
                
                String resultVar = generateUniqueVariable();
                
                List<String> processedArgs = new ArrayList<>();
                for (String arg : functionCall.getArguments()) {
                    if (currentResultVar != null && arg.equals(currentResultVar)) {
                        processedArgs.add(currentResultVar);
                    } else if (arg.startsWith("z_func_")) {
                        int funcIndex = Integer.parseInt(arg.substring(7));
                        if (funcIndex < i) {
                            processedArgs.add(currentResultVar != null ? currentResultVar : arg);
                        } else {
                            processedArgs.add(arg);
                        }
                    } else {
                        if (arg.startsWith("(") && arg.endsWith(")")) {
                            String nestedResult = executeCompositionVirtual(arg, context);
                            processedArgs.add(nestedResult);
                        } else {
                            processedArgs.add(arg);
                        }
                    }
                }
                
                executeSingleFunctionVirtual(functionCall.getFunctionName(), processedArgs, resultVar, context);
                
                currentResultVar = resultVar;
            }
            
            return currentResultVar;
            
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to execute function composition virtually: " + composition, e);
        }
    }
    
    private void executeSingleFunctionVirtual(String functionName, List<String> args, String resultVar, 
                                            engine.execution.ExecutionContext context) {
        if (functionName == null || functionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        if (resultVar == null || resultVar.trim().isEmpty()) {
            throw new IllegalArgumentException("Result variable cannot be null or empty");
        }
        if (context == null) {
            throw new IllegalArgumentException("Execution context cannot be null");
        }
        
        engine.api.SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
        if (functionProgram == null) {
            throw new UnsupportedOperationException("Function not found: " + functionName);
        }
        
        List<String> inputVariables = functionProgram.getInputVariables();
        if (args.size() != inputVariables.size()) {
            throw new UnsupportedOperationException("Argument count mismatch for function " + functionName + 
                ": expected " + inputVariables.size() + ", got " + args.size());
        }
        
        executeFunctionCallVirtual(functionProgram, args, resultVar, context);
    }
    
    private String generateUniqueVariable() {
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return "z_temp_" + timestamp + "_" + random;
    }
    
    private void executeFunctionCallVirtual(engine.api.SProgram functionProgram, List<String> functionArgs, 
                                          String resultVariable, engine.execution.ExecutionContext context) {
        engine.execution.ExecutionContext functionContext = createFunctionExecutionContext(functionProgram, functionArgs, context);
        
        executeFunctionProgram(functionProgram, functionContext);
        
        int result = functionContext.getVariableManager().getYValue();
        context.getVariableManager().setValue(resultVariable, result);
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
    
    private void executeFunctionProgram(engine.api.SProgram functionProgram, 
                                      engine.execution.ExecutionContext functionContext) {
        engine.execution.ProgramRunner runner = new engine.execution.ProgramRunner();
        
        try {
            List<engine.api.SInstruction> instructions = functionProgram.getInstructions();
            while (!functionContext.isProgramTerminated() && 
                   functionContext.getCurrentInstructionIndex() < instructions.size()) {
                runner.executeSingleInstruction(functionProgram, functionContext);
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error executing function program", e);
        }
    }
    
    private Map<String, Integer> buildLabelToIndexMap(List<engine.api.SInstruction> instructions) {
        Map<String, Integer> labelToIndexMap = new HashMap<>();
        
        for (int i = 0; i < instructions.size(); i++) {
            String label = instructions.get(i).getLabel();
            if (label != null && !label.trim().isEmpty()) {
                labelToIndexMap.put(label.trim(), i);
            }
        }
        
        return labelToIndexMap;
    }
}
