package engine.model.instruction;

import engine.api.SInstruction;
import engine.model.InstructionType;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class BaseInstruction implements SInstruction {
    protected final String name;
    protected final InstructionType type;
    protected final String variable;
    protected final String label;
    protected final Map<String, String> arguments;
    protected final int cycles;

    protected final SInstruction sourceInstruction;
    protected final int originalLineNumber;

    protected BaseInstruction(String name, InstructionType type, String variable, 
                            String label, Map<String, String> arguments, int cycles) {
        this(name, type, variable, label, arguments, cycles, null, -1);
    }

    protected BaseInstruction(String name, InstructionType type, String variable, 
                            String label, Map<String, String> arguments, int cycles,
                            SInstruction sourceInstruction) {
        this(name, type, variable, label, arguments, cycles, sourceInstruction, 
             sourceInstruction != null ? getOriginalLineNumber(sourceInstruction) : -1);
    }

    protected BaseInstruction(String name, InstructionType type, String variable, 
                            String label, Map<String, String> arguments, int cycles,
                            SInstruction sourceInstruction, int originalLineNumber) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Instruction name cannot be null or empty");
        }
        if (type == null) {
            throw new IllegalArgumentException("Instruction type cannot be null");
        }
        if (variable == null || variable.trim().isEmpty()) {
            throw new IllegalArgumentException("Variable cannot be null or empty");
        }
        if (cycles < 0) {
            throw new IllegalArgumentException("Cycles cannot be negative");
        }


        this.name = name.trim();
        this.type = type;
        this.variable = variable.trim();
        this.label = (label != null && !label.trim().isEmpty()) ? label.trim() : null;
        this.arguments = (arguments != null) ? Map.copyOf(arguments) : Map.of();
        this.cycles = cycles;

        this.sourceInstruction = sourceInstruction;
        this.originalLineNumber = originalLineNumber;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public InstructionType getType() {
        return type;
    }

    @Override
    public String getVariable() {
        return variable;
    }

    @Override
    public String getLabel() {
        return label;
    }

    @Override
    public Map<String, String> getArguments() {
        return arguments;
    }

    @Override
    public int getCycles() {
        return cycles;
    }



    @Override
    public SInstruction getSourceInstruction() {
        return sourceInstruction;
    }

    public int getOriginalLineNumber() {
        return originalLineNumber;
    }

    @Override
    public List<SInstruction> getAncestryChain() {
        List<SInstruction> chain = new ArrayList<>();
        SInstruction current = this;
        while (current != null) {
            chain.add(current);
            current = current.getSourceInstruction();
        }
        return chain;
    }

    @Override
    public abstract List<SInstruction> expand(ExpansionContext context);

    @Override
    public final void execute(ExecutionContext context) {
        int instructionPointerBefore = context.getCurrentInstructionIndex();
        
        executeInstruction(context);

        if (context.getCurrentInstructionIndex() == instructionPointerBefore) {
            context.incrementInstructionPointer();
        }
    }
    
    protected abstract void executeInstruction(ExecutionContext context);

    @Override
    public abstract String getDisplayFormat();

    protected String formatLabel() {
        if (label != null) {
            return String.format("[ %-3s ]", label);
        } else {
            return "[     ]";
        }
    }

    protected String formatTypeIndicator() {
        return type == InstructionType.BASIC ? "(B)" : "(S)";
    }

    @Override
    public String toString() {
        return String.format("%-4s %s %-25s (%d)", 
                           formatTypeIndicator(), formatLabel(), 
                           getDisplayFormat(), cycles);
    }

    @Override
    public String toStringWithHistory(int currentLineNumber) {
        String baseFormat = toString();
        List<SInstruction> ancestors = getAncestryChain();
        
        if (ancestors.size() <= 1) {
            return baseFormat;
        }
        
        StringBuilder result = new StringBuilder(baseFormat);
        
        for (int i = 1; i < ancestors.size(); i++) {
            SInstruction ancestor = ancestors.get(i);
            String ancestorLabel = ancestor.getLabel() != null ? 
                String.format("[ %-3s ]", ancestor.getLabel()) : "[     ]";
            String ancestorType = ancestor.getType() == InstructionType.BASIC ? "(B)" : "(S)";
            
            int ancestorLineNumber = getAncestorOriginalLineNumber(ancestor, currentLineNumber);
            result.append(String.format(" <<< #%d %s %s %-25s (%d)", 
                ancestorLineNumber, ancestorType, ancestorLabel, 
                ancestor.getDisplayFormat(), ancestor.getCycles()));
        }
        
        return result.toString();
    }
    
    @Override
    public String toStringWithMultiLevelHistory(List<engine.expansion.MultiLevelExpansion.InstructionAncestor> ancestry) {
        String baseFormat = toString();
        
        if (ancestry == null || ancestry.isEmpty()) {
            return baseFormat;
        }
        
        StringBuilder result = new StringBuilder(baseFormat);
        
        for (engine.expansion.MultiLevelExpansion.InstructionAncestor ancestor : ancestry) {
            String ancestorLabel = ancestor.instruction.getLabel() != null ? 
                String.format("[ %-3s ]", ancestor.instruction.getLabel()) : "[     ]";
            String ancestorType = ancestor.instruction.getType() == InstructionType.BASIC ? "(B)" : "(S)";
            
            result.append(String.format(" <<< #%d %s %s %-25s (%d)", 
                ancestor.lineNumber, ancestorType, ancestorLabel, 
                ancestor.instruction.getDisplayFormat(), ancestor.instruction.getCycles()));
        }
        
        return result.toString();
    }
    
    private static int getOriginalLineNumber(SInstruction instruction) {
        if (instruction == null) {
            return -1;
        }
        if (instruction instanceof BaseInstruction) {
            BaseInstruction base = (BaseInstruction) instruction;
            return base.originalLineNumber != -1 ? base.originalLineNumber : -1;
        }
        return -1;
    }
    
    private static int getAncestorOriginalLineNumber(SInstruction ancestor, int fallbackLineNumber) {
        if (ancestor instanceof BaseInstruction) {
            BaseInstruction base = (BaseInstruction) ancestor;
            if (base.originalLineNumber != -1) {
                return base.originalLineNumber;
            }
        }
        return fallbackLineNumber;
    }
}
