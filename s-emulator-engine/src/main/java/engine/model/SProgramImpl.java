package engine.model;

import engine.api.SInstruction;
import engine.api.SProgram;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class SProgramImpl implements SProgram {
    private final String name;
    private final List<SInstruction> instructions;
    private List<String> cachedInputVariables;
    private List<String> cachedLabels;
    private Integer cachedMaxExpansionLevel;
    private FunctionRegistry functionRegistry;

    public SProgramImpl(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Program name cannot be null or empty");
        }
        this.name = name.trim();
        this.instructions = new ArrayList<>();
        this.cachedInputVariables = null;
        this.cachedLabels = null;
        this.cachedMaxExpansionLevel = null;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<SInstruction> getInstructions() {
        return List.copyOf(instructions);
    }

    @Override
    public List<String> getInputVariables() {
        if (cachedInputVariables == null) {
            cachedInputVariables = calculateInputVariables();
        }
        return cachedInputVariables;
    }

    @Override
    public List<String> getLabels() {
        if (cachedLabels == null) {
            cachedLabels = calculateLabels();
        }
        return cachedLabels;
    }

    @Override
    public int getMaxExpansionLevel() {
        if (cachedMaxExpansionLevel == null) {
            cachedMaxExpansionLevel = calculateMaxExpansionLevel();
        }
        return cachedMaxExpansionLevel;
    }

    @Override
    public SProgram expandToLevel(int level) {
        if (level < 0) {
            throw new IllegalArgumentException("Expansion level cannot be negative: " + level);
        }
        if (level == 0) {
            return this;
        }
        
        return this;
    }

    public void addInstruction(SInstruction instruction) {
        if (instruction == null) {
            throw new IllegalArgumentException("Instruction cannot be null");
        }
        instructions.add(instruction);
        invalidateCache();
    }

    private void invalidateCache() {
        cachedInputVariables = null;
        cachedLabels = null;
        cachedMaxExpansionLevel = null;
    }

    private List<String> calculateInputVariables() {
        Set<String> inputVariables = new HashSet<>();
        
        for (SInstruction instruction : instructions) {
            String variable = instruction.getVariable();
            if (variable != null && SEmulatorConstants.X_VARIABLE_PATTERN.matcher(variable).matches()) {
                inputVariables.add(variable);
            }
            
            for (String argValue : instruction.getArguments().values()) {
                if (argValue != null && SEmulatorConstants.X_VARIABLE_PATTERN.matcher(argValue).matches()) {
                    inputVariables.add(argValue);
                }
            }
        }
        
        return inputVariables.stream()
                .sorted((a, b) -> {
                    int numA = Integer.parseInt(a.substring(1));
                    int numB = Integer.parseInt(b.substring(1));
                    return Integer.compare(numA, numB);
                })
                .collect(Collectors.toList());
    }

    private List<String> calculateLabels() {
        Set<String> labels = new HashSet<>();
        
        for (SInstruction instruction : instructions) {
            String label = instruction.getLabel();
            if (label != null && !label.trim().isEmpty()) {
                labels.add(label.trim());
            }
        }
        
        return labels.stream()
                .sorted((a, b) -> {
                    if (a.equals(SEmulatorConstants.EXIT_LABEL)) return 1;
                    if (b.equals(SEmulatorConstants.EXIT_LABEL)) return -1;
                    
                    if (a.startsWith("L") && b.startsWith("L")) {
                        try {
                            int numA = Integer.parseInt(a.substring(1));
                            int numB = Integer.parseInt(b.substring(1));
                            return Integer.compare(numA, numB);
                        } catch (NumberFormatException e) {
                            return a.compareTo(b);
                        }
                    }
                    return a.compareTo(b);
                })
                .collect(Collectors.toList());
    }

    private int calculateMaxExpansionLevel() {
        int maxLevel = 0;
        
        for (SInstruction instruction : instructions) {
            if (instruction.getType() == InstructionType.SYNTHETIC) {
                int instructionLevel = calculateDeepExpansionLevel(instruction);
                maxLevel = Math.max(maxLevel, instructionLevel);
            }
        }
        
        return maxLevel;
    }
    
    private int calculateDeepExpansionLevel(SInstruction instruction) {
        int baseLevel = getInstructionExpansionLevel(instruction.getName());
        
        // For QUOTE instructions with function compositions, we need one additional level
        // because the composition creates new synthetic instructions that need expansion
        if (instruction.getName().equals(SEmulatorConstants.QUOTE_NAME)) {
            String functionArguments = instruction.getArguments().get(SEmulatorConstants.FUNCTION_ARGUMENTS_ARG);
            
            // If we have nested function compositions, we need additional levels
            if (functionArguments != null && hasNestedFunctionComposition(functionArguments)) {
                baseLevel = Math.max(baseLevel, 6); // QUOTE with nested composition needs more levels
            }
        }
        
        return baseLevel;
    }
    
    private boolean hasNestedFunctionComposition(String functionArguments) {
        // Check if arguments contain nested function calls like (NOT,(EQUAL,...))
        if (!functionArguments.contains("(") || !functionArguments.contains(")")) {
            return false;
        }
        
        // Count nested parentheses to detect composition depth
        int depth = 0;
        int maxDepth = 0;
        for (char c : functionArguments.toCharArray()) {
            if (c == '(') {
                depth++;
                maxDepth = Math.max(maxDepth, depth);
            } else if (c == ')') {
                depth--;
            }
        }
        
        // If we have nesting depth > 1, it's a nested composition
        return maxDepth > 1;
    }
    

    private int getInstructionExpansionLevel(String instructionName) {
        switch (instructionName) {
            case SEmulatorConstants.ZERO_VARIABLE_NAME:
            case SEmulatorConstants.GOTO_LABEL_NAME:
                return 1;
            
            case SEmulatorConstants.ASSIGNMENT_NAME:
            case SEmulatorConstants.CONSTANT_ASSIGNMENT_NAME:
            case SEmulatorConstants.JUMP_ZERO_NAME:
                return 2;
            
            case SEmulatorConstants.JUMP_EQUAL_CONSTANT_NAME:
            case SEmulatorConstants.JUMP_EQUAL_VARIABLE_NAME:
            case SEmulatorConstants.QUOTE_NAME:
                return 3;
            
            case SEmulatorConstants.JUMP_EQUAL_FUNCTION_NAME:
                return 4;
            
            default:
                throw new IllegalArgumentException("Unknown synthetic instruction: " + instructionName);
        }
    }

    public boolean isEmpty() {
        return instructions.isEmpty();
    }

    public int getInstructionCount() {
        return instructions.size();
    }

    public void validate() {
        if (instructions.isEmpty()) {
            throw new IllegalStateException("Program must contain at least one instruction");
        }
        
        Set<String> definedLabels = new HashSet<>();
        Set<String> referencedLabels = new HashSet<>();
        
        for (SInstruction instruction : instructions) {
            String label = instruction.getLabel();
            if (label != null && !label.trim().isEmpty()) {
                String trimmedLabel = label.trim();
                if (definedLabels.contains(trimmedLabel)) {
                    throw new IllegalStateException("Duplicate label found: " + trimmedLabel);
                }
                definedLabels.add(trimmedLabel);
            }
            
            for (String argValue : instruction.getArguments().values()) {
                if (argValue != null && SEmulatorConstants.LABEL_PATTERN.matcher(argValue).matches()) {
                    referencedLabels.add(argValue);
                }
            }
        }
        
        for (String referencedLabel : referencedLabels) {
            if (!definedLabels.contains(referencedLabel) && !referencedLabel.equals(SEmulatorConstants.EXIT_LABEL)) {
                throw new IllegalStateException("Referenced label '" + referencedLabel + "' is not defined in the program");
            }
        }
    }
    
    @Override
    public FunctionRegistry getFunctionRegistry() {
        return functionRegistry;
    }
    
    @Override
    public void setFunctionRegistry(FunctionRegistry functionRegistry) {
        this.functionRegistry = functionRegistry;
    }
}
