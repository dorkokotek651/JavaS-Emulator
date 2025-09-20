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
    
    public GotoLabelInstruction(String variable, String label, Map<String, String> arguments,
                              SInstruction sourceInstruction) {
        super(SEmulatorConstants.GOTO_LABEL_NAME, InstructionType.SYNTHETIC, variable, label, arguments, 
              SEmulatorConstants.GOTO_LABEL_CYCLES, sourceInstruction);
        
        if (arguments == null || !arguments.containsKey(SEmulatorConstants.GOTO_LABEL_ARG)) {
            throw new IllegalArgumentException("GOTO_LABEL instruction requires 'gotoLabel' argument");
        }
        
        this.gotoLabel = arguments.get(SEmulatorConstants.GOTO_LABEL_ARG);
        if (gotoLabel == null || gotoLabel.trim().isEmpty()) {
            throw new IllegalArgumentException("gotoLabel cannot be null or empty");
        }
    }

    @Override
    protected void executeInstruction(ExecutionContext context) {
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
            Map.of(),
            this
        );
        
        JumpNotZeroInstruction jumpInstruction = new JumpNotZeroInstruction(
            workingVariable,
            null,
            Map.of(SEmulatorConstants.JNZ_LABEL_ARG, gotoLabel),
            this
        );
        
        expandedInstructions.add(increaseInstruction);
        expandedInstructions.add(jumpInstruction);
        
        return expandedInstructions;
    }

    public String getGotoLabel() {
        return gotoLabel;
    }

}
