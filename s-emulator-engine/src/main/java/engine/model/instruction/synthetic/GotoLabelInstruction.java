package engine.model.instruction.synthetic;

import engine.api.SInstruction;
import engine.model.instruction.BaseInstruction;
import engine.model.instruction.basic.IncreaseInstruction;
import engine.model.instruction.basic.JumpNotZeroInstruction;
import engine.model.InstructionType;
import engine.model.SEmulatorConstants;
import engine.expansion.ExpansionContext;
import engine.execution.ExecutionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GotoLabelInstruction extends BaseInstruction {
    private final String gotoLabel;
    
    public GotoLabelInstruction(String variable, String label, Map<String, String> arguments) {
        super("GOTO_LABEL", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.GOTO_LABEL_CYCLES);
        
        if (arguments == null || !arguments.containsKey("gotoLabel")) {
            throw new IllegalArgumentException("GOTO_LABEL instruction requires 'gotoLabel' argument");
        }
        
        this.gotoLabel = arguments.get("gotoLabel");
        if (gotoLabel == null || gotoLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("gotoLabel cannot be null or empty");
        }
    }

    public GotoLabelInstruction(String variable, String label, Map<String, String> arguments,
                              int expansionLevel, SInstruction sourceInstruction) {
        super("GOTO_LABEL", InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.GOTO_LABEL_CYCLES, expansionLevel, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey("gotoLabel")) {
            throw new IllegalArgumentException("GOTO_LABEL instruction requires 'gotoLabel' argument");
        }
        
        this.gotoLabel = arguments.get("gotoLabel");
        if (gotoLabel == null || gotoLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("gotoLabel cannot be null or empty");
        }
    }

    @Override
    public void execute(ExecutionContext context) {
        context.addCycles(cycles);
        context.jumpToLabel(gotoLabel);
    }

    @Override
    public String getDisplayFormat() {
        return "GOTO " + gotoLabel;
    }

    @Override
    public List<SInstruction> expand(ExpansionContext context) {
        List<SInstruction> expandedInstructions = new ArrayList<>();
        
        String workingVariable = context.getUniqueWorkingVariable();
        
        IncreaseInstruction increaseInstruction = new IncreaseInstruction(
            workingVariable,
            null,
            Map.of()
        );
        
        JumpNotZeroInstruction jumpInstruction = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of("JNZLabel", gotoLabel)
        );
        
        expandedInstructions.add(increaseInstruction);
        expandedInstructions.add(jumpInstruction);
        
        return expandedInstructions;
    }

    public String getGotoLabel() {
        return gotoLabel;
    }
    
    @Override
    protected int calculateExpansionLevel() {
        return 1;
    }
    
    @Override
    protected List<SInstruction> createDependencies(ExpansionContext context) {
        String workingVariable = context.getUniqueWorkingVariable();
        
        return List.of(
            new IncreaseInstruction(workingVariable, null, Map.of()),
            new JumpNotZeroInstruction(workingVariable, null, Map.of("JNZLabel", gotoLabel))
        );
    }
}
