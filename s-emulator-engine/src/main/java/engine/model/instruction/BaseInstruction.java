package engine.model.instruction;

import engine.api.SInstruction;
import engine.model.InstructionType;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
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

    protected BaseInstruction(String name, InstructionType type, String variable, 
                            String label, Map<String, String> arguments, int cycles) {
        this(name, type, variable, label, arguments, cycles, null);
    }

    protected BaseInstruction(String name, InstructionType type, String variable, 
                            String label, Map<String, String> arguments, int cycles,
                            SInstruction sourceInstruction) {
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
        return String.format("%-4s %s %-25s %-20s (%d)", 
                           formatTypeIndicator(), formatLabel(), 
                           getDisplayFormat(), getName(), cycles);
    }
}
