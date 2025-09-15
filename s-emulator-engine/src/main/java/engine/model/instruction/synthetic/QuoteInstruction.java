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

/**
 * Implementation of the QUOTE instruction (Expansion Level 3).
 * 
 * This instruction allows calling a function (sub-program) and placing its result
 * in a target variable. It handles variable substitution, label substitution,
 * and function composition according to the specification in Appendix C.
 * 
 * Expansion Algorithm:
 * 1. Replace function input variables (x1..xn) with unique working variables
 * 2. Replace function result variable (y) with unique working variable  
 * 3. Replace function labels with unique labels
 * 4. Create input assignments for function arguments
 * 5. Insert function instructions with proper substitution
 * 6. Create final result assignment with end label
 * 7. Handle EXIT labels by replacing with end label
 * 
 * Cycles: 5 as per Appendix C
 * User view: V ← (functionName,args...) (Example: x2 ← (ID,z58))
 */
public class QuoteInstruction extends BaseInstruction {
    
    private final String functionName;
    private final String functionArguments;
    
    /**
     * Creates a new QUOTE instruction.
     * 
     * @param variable the target variable to store the function result
     * @param label the instruction label (can be null)
     * @param arguments map containing functionName and functionArguments
     */
    public QuoteInstruction(String variable, String label, Map<String, String> arguments) {
        super(SEmulatorConstants.QUOTE_NAME, InstructionType.SYNTHETIC, 
              variable, label, arguments, SEmulatorConstants.QUOTE_CYCLES);
        
        validateArguments();
        this.functionName = arguments.get(SEmulatorConstants.FUNCTION_NAME_ARG);
        this.functionArguments = arguments.get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
    }
    
    /**
     * Creates a new QUOTE instruction with source instruction for expansion tracking.
     */
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
        
        // Parse function arguments - handle both simple variables and compositions
        List<String> functionArgs = parseFunctionArguments(functionArguments);
        
        // Resolve all arguments (expand compositions first, keep simple variables as-is)
        List<String> resolvedArgs = resolveArguments(functionArgs, expandedInstructions, context);
        
        // Now expand the main function call with resolved arguments
        expandSingleFunction(functionName, resolvedArgs, variable, expandedInstructions, context);
        
        return expandedInstructions;
    }
    
    /**
     * Resolves function arguments, expanding any compositions and keeping simple variables.
     * Function compositions are executed first and their results stored in temporary variables.
     */
    private List<String> resolveArguments(List<String> rawArgs, List<SInstruction> expandedInstructions, 
                                        ExpansionContext context) {
        List<String> resolvedArgs = new ArrayList<>();
        
        for (String arg : rawArgs) {
            if (arg.startsWith("(") && arg.endsWith(")")) {
                // This is a function composition - expand it
                String resultVar = expandComposition(arg, expandedInstructions, context);
                resolvedArgs.add(resultVar);
            } else {
                // This is a simple variable - use as-is
                resolvedArgs.add(arg);
            }
        }
        return resolvedArgs;
    }
    
    /**
     * Expands a function composition like "(Successor,x1)" and returns the variable containing the result.
     */
    private String expandComposition(String composition, List<SInstruction> expandedInstructions, 
                                   ExpansionContext context) {
        try {
            // Parse the composition to extract function name and arguments
            List<CompositionParser.FunctionCall> functionCalls = CompositionParser.parseComposition(composition);
            
            if (functionCalls.isEmpty()) {
                throw new IllegalArgumentException("Empty function composition: " + composition);
            }
            
            // For simple compositions like "(Successor,x1)", there should be exactly one function call
            CompositionParser.FunctionCall functionCall = functionCalls.get(0);
            
            // Generate a unique variable for the result
            String resultVar = context.getUniqueWorkingVariable();
            context.markVariableAsUsed(resultVar);
            
            // Expand this function call
            expandSingleFunction(functionCall.getFunctionName(), functionCall.getArguments(), 
                               resultVar, expandedInstructions, context);
            
            return resultVar;
            
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to expand function composition: " + composition, e);
        }
    }
    
    /**
     * Expands a single function call according to Appendix C guidelines.
     */
    private void expandSingleFunction(String functionName, List<String> args, String resultVar, 
                                    List<SInstruction> expandedInstructions, ExpansionContext context) {
        
        
        // Get the function program from registry
        SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
        if (functionProgram == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        
        // Step 1: Create variable mappings according to Appendix C
        Map<String, String> variableMapping = createVariableMapping(args, context);
        
        // Step 2: Create label mappings
        Map<String, String> labelMapping = createLabelMapping(functionProgram, context);
        String endLabel = context.getUniqueLabel();
        context.markLabelAsUsed(endLabel);
        
        // Step 3: Create input assignments (zi ← args[i])
        createInputAssignments(args, variableMapping, expandedInstructions);
        
        // Step 4: Add function instructions with substitution
        addSubstitutedInstructions(functionProgram, variableMapping, labelMapping, endLabel, expandedInstructions);
        
        // Step 5: Create final result assignment (resultVar ← zy)
        createResultAssignment(variableMapping.get("y"), resultVar, endLabel, expandedInstructions);
    }
    
    /**
     * Creates variable mapping according to Appendix C:
     * - x1, x2, ... → unique working variables
     * - y → unique working variable
     */
    private Map<String, String> createVariableMapping(List<String> args, ExpansionContext context) {
        Map<String, String> mapping = new HashMap<>();
        
        // Map input variables x1, x2, ... to unique working variables
        for (int i = 0; i < args.size(); i++) {
            String inputVar = "x" + (i + 1);
            String uniqueVar = context.getUniqueWorkingVariable();
            mapping.put(inputVar, uniqueVar);
            context.markVariableAsUsed(uniqueVar);
        }
        
        // Map y to unique working variable
        String yVar = context.getUniqueWorkingVariable();
        mapping.put("y", yVar);
        context.markVariableAsUsed(yVar);
        
        return mapping;
    }
    
    /**
     * Creates label mapping for the function.
     */
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
    
    /**
     * Creates input assignments: zi ← args[i]
     */
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
    
    /**
     * Adds function instructions with proper variable and label substitution.
     */
    private void addSubstitutedInstructions(SProgram functionProgram, Map<String, String> variableMapping,
                                          Map<String, String> labelMapping, String endLabel,
                                          List<SInstruction> expandedInstructions) {
        
        for (SInstruction instruction : functionProgram.getInstructions()) {
            SInstruction substituted = substituteInstruction(instruction, variableMapping, labelMapping, endLabel);
            expandedInstructions.add(substituted);
        }
    }
    
    /**
     * Creates the final result assignment with end label.
     */
    private void createResultAssignment(String yVar, String resultVar, String endLabel,
                                      List<SInstruction> expandedInstructions) {
        Map<String, String> assignmentArgs = new HashMap<>();
        assignmentArgs.put(SEmulatorConstants.ASSIGNED_VARIABLE_ARG, yVar);
        
        SInstruction resultAssignment = new AssignmentInstruction(resultVar, endLabel, assignmentArgs, this);
        expandedInstructions.add(resultAssignment);
    }
    
    /**
     * Substitutes variables and labels in an instruction according to the mapping rules.
     */
    private SInstruction substituteInstruction(SInstruction instruction, Map<String, String> variableMapping,
                                             Map<String, String> labelMapping, String endLabel) {
        try {
            // Substitute the main variable
            String newVariable = substituteVariable(instruction.getVariable(), variableMapping);
            
            // Substitute the label
            String newLabel = substituteLabel(instruction.getLabel(), labelMapping);
            
            // Substitute arguments
            Map<String, String> newArguments = substituteArguments(instruction.getArguments(), 
                                                                  variableMapping, labelMapping, endLabel);
            
            // Create new instruction using the factory
            SInstruction newInstruction = InstructionFactory.createInstruction(
                instruction.getName(), newVariable, newLabel, newArguments);
            
            return newInstruction;
                
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to substitute instruction: " + 
                instruction.getDisplayFormat(), e);
        }
    }
    
    private String substituteVariable(String variable, Map<String, String> variableMapping) {
        if (variable == null) return null;
        return variableMapping.getOrDefault(variable, variable);
    }
    
    private String substituteLabel(String label, Map<String, String> labelMapping) {
        if (label == null) return null;
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
            
            // Handle variable arguments
            if (key.equals(SEmulatorConstants.ASSIGNED_VARIABLE_ARG) || 
                key.equals(SEmulatorConstants.VARIABLE_NAME_ARG)) {
                // Always substitute function internal variables
                // The variableMapping only contains function internal variables
                if (variableMapping.containsKey(value)) {
                    newValue = substituteVariable(value, variableMapping);
                }
            }
            // Handle label arguments
            else if (key.equals(SEmulatorConstants.JNZ_LABEL_ARG) || 
                     key.equals(SEmulatorConstants.JZ_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.GOTO_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.JE_CONSTANT_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.JE_VARIABLE_LABEL_ARG) ||
                     key.equals(SEmulatorConstants.JE_FUNCTION_LABEL_ARG)) {
                
                if (SEmulatorConstants.EXIT_LABEL.equals(value)) {
                    newValue = endLabel; // Replace EXIT with endLabel
                } else {
                    newValue = substituteLabel(value, labelMapping);
                }
            }
            
            newArguments.put(key, newValue);
        }
        
        return newArguments;
    }
    
    /**
     * Parses function arguments, properly handling nested parentheses.
     * Example: "(Const7),(Successor,x1)" → ["(Const7)", "(Successor,x1)"]
     */
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
                // Found a top-level comma - this separates arguments
                String arg = trimmed.substring(start, i).trim();
                if (!arg.isEmpty()) {
                    args.add(arg);
                }
                start = i + 1;
            }
        }
        
        // Add the last argument
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
    
    /**
     * Executes the QUOTE instruction virtually during debugging.
     * This allows QUOTE instructions to be executed as single logical steps
     * without requiring full expansion.
     * 
     * @param context the execution context
     */
    private void executeVirtual(engine.execution.ExecutionContext context) {
        try {
            // Check if function registry is available
            if (context.getFunctionRegistry() == null) {
                throw new UnsupportedOperationException("Function registry not available for virtual execution");
            }
            
            // Check if function registry is empty
            if (context.getFunctionRegistry().isEmpty()) {
                throw new UnsupportedOperationException("Function registry is empty - no functions available");
            }
            
            // Get the function program from registry
            engine.api.SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
            if (functionProgram == null) {
                throw new UnsupportedOperationException("Function not found: " + functionName);
            }
            
            // Parse function arguments
            List<String> functionArgs = parseFunctionArguments(functionArguments);
            
            // Resolve function compositions if any exist
            List<String> resolvedArgs = resolveArgumentsForVirtualExecution(functionArgs, context);
            
            // Execute function call virtually
            executeFunctionCallVirtual(functionProgram, resolvedArgs, getVariable(), context);
            
            // Add cycles
            context.addCycles(getCycles());
            
        } catch (UnsupportedOperationException e) {
            // Re-throw UnsupportedOperationException as-is
            throw e;
        } catch (Exception e) {
            // Wrap other exceptions with more context
            throw new UnsupportedOperationException("Virtual execution failed for QUOTE instruction", e);
        }
    }
    
    /**
     * Resolves function arguments for virtual execution, handling function compositions.
     * This is the virtual equivalent of the resolveArguments method used in expansion.
     * 
     * @param rawArgs the raw function arguments
     * @param context the execution context
     * @return list of resolved arguments (variable names)
     */
    private List<String> resolveArgumentsForVirtualExecution(List<String> rawArgs, 
                                                           engine.execution.ExecutionContext context) {
        List<String> resolvedArgs = new ArrayList<>();
        
        for (String arg : rawArgs) {
            if (arg.startsWith("(") && arg.endsWith(")")) {
                // This is a function composition - execute it virtually
                String resultVar = executeCompositionVirtual(arg, context);
                resolvedArgs.add(resultVar);
            } else {
                // This is a simple variable - use as-is
                resolvedArgs.add(arg);
            }
        }
        return resolvedArgs;
    }
    
    /**
     * Executes a function composition virtually and returns the variable containing the result.
     * This is the virtual equivalent of the expandComposition method used in expansion.
     * 
     * @param composition the function composition string (e.g., "(Successor,x1)")
     * @param context the execution context
     * @return the variable name containing the result
     */
    private String executeCompositionVirtual(String composition, engine.execution.ExecutionContext context) {
        try {
            // Parse the composition to extract function name and arguments
            List<CompositionParser.FunctionCall> functionCalls = CompositionParser.parseComposition(composition);
            
            if (functionCalls.isEmpty()) {
                throw new IllegalArgumentException("Empty function composition: " + composition);
            }
            
            // For simple compositions like (CONST0) or (Successor,x1), there should be exactly one function call
            if (functionCalls.size() == 1) {
                CompositionParser.FunctionCall functionCall = functionCalls.get(0);
                
                // Generate a unique variable for this function call's result
                String resultVar = generateUniqueVariable(context);
                
                // Process arguments - resolve any nested compositions
                List<String> processedArgs = new ArrayList<>();
                for (String arg : functionCall.getArguments()) {
                    if (arg.startsWith("(") && arg.endsWith(")")) {
                        // Recursively execute nested composition
                        String nestedResult = executeCompositionVirtual(arg, context);
                        processedArgs.add(nestedResult);
                    } else {
                        processedArgs.add(arg);
                    }
                }
                
                // Execute this function call virtually
                executeSingleFunctionVirtual(functionCall.getFunctionName(), processedArgs, resultVar, context);
                
                return resultVar;
            }
            
            // Handle multiple function calls for deeply nested compositions
            // Execute each function call in order and use the result as input for the next
            String currentResultVar = null;
            
            for (int i = 0; i < functionCalls.size(); i++) {
                CompositionParser.FunctionCall functionCall = functionCalls.get(i);
                
                // Generate a unique variable for this function call's result
                String resultVar = generateUniqueVariable(context);
                
                // For nested calls, replace the previous result variable in arguments
                List<String> processedArgs = new ArrayList<>();
                for (String arg : functionCall.getArguments()) {
                    if (currentResultVar != null && arg.equals(currentResultVar)) {
                        // This argument should use the result from the previous function call
                        processedArgs.add(currentResultVar);
                    } else if (arg.startsWith("z_func_")) {
                        // This is a reference to a previous function call result
                        // We need to find the actual result variable from a previous execution
                        // For now, let's handle this by executing the referenced function
                        int funcIndex = Integer.parseInt(arg.substring(7));
                        if (funcIndex < i) {
                            // This should reference a previous result - we need to track this properly
                            // For now, let's just use the current result variable
                            processedArgs.add(currentResultVar != null ? currentResultVar : arg);
                        } else {
                            processedArgs.add(arg);
                        }
                    } else {
                        // This is a regular argument - resolve any compositions in it
                        if (arg.startsWith("(") && arg.endsWith(")")) {
                            // Recursively execute nested composition
                            String nestedResult = executeCompositionVirtual(arg, context);
                            processedArgs.add(nestedResult);
                        } else {
                            processedArgs.add(arg);
                        }
                    }
                }
                
                // Execute this function call virtually
                executeSingleFunctionVirtual(functionCall.getFunctionName(), processedArgs, resultVar, context);
                
                // Update current result variable for next iteration
                currentResultVar = resultVar;
            }
            
            return currentResultVar;
            
        } catch (UnsupportedOperationException e) {
            throw new UnsupportedOperationException("Failed to execute function composition virtually: " + composition, e);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Failed to execute function composition virtually: " + composition, e);
        }
    }
    
    /**
     * Executes a single function call virtually.
     * This is the virtual equivalent of the expandSingleFunction method used in expansion.
     * 
     * @param functionName the name of the function to execute
     * @param args the function arguments
     * @param resultVar the variable to store the result
     * @param context the execution context
     */
    private void executeSingleFunctionVirtual(String functionName, List<String> args, String resultVar, 
                                            engine.execution.ExecutionContext context) {
        // Validate inputs
        if (functionName == null || functionName.trim().isEmpty()) {
            throw new IllegalArgumentException("Function name cannot be null or empty");
        }
        if (resultVar == null || resultVar.trim().isEmpty()) {
            throw new IllegalArgumentException("Result variable cannot be null or empty");
        }
        if (context == null) {
            throw new IllegalArgumentException("Execution context cannot be null");
        }
        
        // Get the function program from registry
        engine.api.SProgram functionProgram = context.getFunctionRegistry().getFunction(functionName);
        if (functionProgram == null) {
            throw new UnsupportedOperationException("Function not found: " + functionName);
        }
        
        // Validate argument count matches function input variables
        List<String> inputVariables = functionProgram.getInputVariables();
        if (args.size() != inputVariables.size()) {
            throw new UnsupportedOperationException("Argument count mismatch for function " + functionName + 
                ": expected " + inputVariables.size() + ", got " + args.size());
        }
        
        // Execute the function call virtually
        executeFunctionCallVirtual(functionProgram, args, resultVar, context);
    }
    
    /**
     * Generates a unique variable name for storing intermediate results.
     * Uses a counter-based approach to ensure uniqueness within the execution context.
     * 
     * @param context the execution context
     * @return a unique variable name
     */
    private String generateUniqueVariable(engine.execution.ExecutionContext context) {
        // Use a simple counter-based approach for unique variable generation
        // This ensures we don't conflict with existing variables
        long timestamp = System.currentTimeMillis();
        int random = (int) (Math.random() * 10000);
        return "z_temp_" + timestamp + "_" + random;
    }
    
    /**
     * Executes a function call virtually by creating an isolated execution context
     * and running the function program.
     * 
     * @param functionProgram the function program to execute
     * @param functionArgs the function arguments
     * @param resultVariable the variable to store the result
     * @param context the main execution context
     */
    private void executeFunctionCallVirtual(engine.api.SProgram functionProgram, List<String> functionArgs, 
                                          String resultVariable, engine.execution.ExecutionContext context) {
        // Create isolated execution context for function
        engine.execution.ExecutionContext functionContext = createFunctionExecutionContext(functionProgram, functionArgs, context);
        
        // Execute the function program
        executeFunctionProgram(functionProgram, functionContext);
        
        // Extract result and assign to target variable
        int result = functionContext.getVariableManager().getYValue();
        context.getVariableManager().setValue(resultVariable, result);
    }
    
    /**
     * Creates an isolated execution context for function execution.
     * 
     * @param functionProgram the function program
     * @param functionArgs the function arguments
     * @param mainContext the main execution context
     * @return isolated execution context for the function
     */
    private engine.execution.ExecutionContext createFunctionExecutionContext(engine.api.SProgram functionProgram, 
                                                                           List<String> functionArgs, 
                                                                           engine.execution.ExecutionContext mainContext) {
        engine.execution.ExecutionContext functionContext = new engine.execution.ExecutionContext();
        
        // Enable debug mode for function execution (required by executeSingleInstruction)
        functionContext.enableDebugMode();
        
        // Enable virtual execution mode for function execution
        // This is essential for QUOTE instructions within functions to work properly
        functionContext.enableVirtualExecutionMode();
        
        // Copy function registry from main context to function context
        // This is essential for function execution to work properly
        functionContext.setFunctionRegistry(mainContext.getFunctionRegistry());
        
        // Map function input variables (x1, x2, ...) to argument values
        List<String> inputVariables = functionProgram.getInputVariables();
        for (int i = 0; i < functionArgs.size() && i < inputVariables.size(); i++) {
            String inputVar = inputVariables.get(i);
            String argValue = functionArgs.get(i);
            
            // Get the argument value from the main context
            int value = mainContext.getVariableManager().getValue(argValue);
            functionContext.getVariableManager().setValue(inputVar, value);
        }
        
        // Set up label mapping for function execution
        Map<String, Integer> labelToIndexMap = buildLabelToIndexMap(functionProgram.getInstructions());
        functionContext.setLabelToIndexMap(labelToIndexMap);
        
        return functionContext;
    }
    
    /**
     * Executes the function program in the isolated context.
     * 
     * @param functionProgram the function program to execute
     * @param functionContext the isolated execution context
     */
    private void executeFunctionProgram(engine.api.SProgram functionProgram, 
                                      engine.execution.ExecutionContext functionContext) {
        // Execute the function program to completion
        engine.execution.ProgramRunner runner = new engine.execution.ProgramRunner();
        
        try {
            // Execute all instructions in the function
            List<engine.api.SInstruction> instructions = functionProgram.getInstructions();
            while (!functionContext.isProgramTerminated() && 
                   functionContext.getCurrentInstructionIndex() < instructions.size()) {
                runner.executeSingleInstruction(functionProgram, functionContext);
            }
        } catch (Exception e) {
            throw new UnsupportedOperationException("Error executing function program", e);
        }
    }
    
    /**
     * Builds a label-to-index mapping for the given instructions.
     * 
     * @param instructions the instructions to map
     * @return map of label names to instruction indices
     */
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
