package engine.execution;

import engine.api.ExecutionResult;
import engine.api.SInstruction;
import engine.api.SProgram;
import engine.exception.ExecutionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramRunner {
    private static final int MAX_EXECUTION_STEPS = 100000;

    public ExecutionResult executeProgram(SProgram program, List<Integer> inputs, int runNumber, int expansionLevel) throws ExecutionException {
        if (program == null) {
            throw new ExecutionException("Program cannot be null");
        }
        if (inputs == null) {
            throw new ExecutionException("Inputs cannot be null");
        }
        if (runNumber <= 0) {
            throw new ExecutionException("Run number must be positive: " + runNumber);
        }
        if (expansionLevel < 0) {
            throw new ExecutionException("Expansion level cannot be negative: " + expansionLevel);
        }

        List<SInstruction> instructions = program.getInstructions();
        if (instructions.isEmpty()) {
            throw new ExecutionException("Program must contain at least one instruction");
        }

        ExecutionContext context = new ExecutionContext();
        context.initializeInputs(inputs);
        
        Map<String, Integer> labelToIndexMap = buildLabelToIndexMap(instructions);
        context.setLabelToIndexMap(labelToIndexMap);

        executeInstructionLoop(instructions, context);

        VariableManager variableManager = context.getVariableManager();
        return new ExecutionResult(
            runNumber,
            expansionLevel,
            inputs,
            variableManager.getYValue(),
            variableManager.getSortedInputVariablesMap(),
            variableManager.getSortedWorkingVariablesMap(),
            context.getTotalCycles(),
            context.getExecutedInstructions()
        );
    }

    private Map<String, Integer> buildLabelToIndexMap(List<SInstruction> instructions) {
        Map<String, Integer> labelToIndexMap = new HashMap<>();
        
        for (int i = 0; i < instructions.size(); i++) {
            SInstruction instruction = instructions.get(i);
            String label = instruction.getLabel();
            if (label != null && !label.trim().isEmpty()) {
                labelToIndexMap.put(label.trim(), i);
            }
        }
        
        return labelToIndexMap;
    }

    private void executeInstructionLoop(List<SInstruction> instructions, ExecutionContext context) throws ExecutionException {
        int executionSteps = 0;
        
        while (!context.isProgramTerminated() && context.getCurrentInstructionIndex() < instructions.size()) {
            if (executionSteps++ > MAX_EXECUTION_STEPS) {
                throw new ExecutionException("Program execution exceeded maximum steps (" + MAX_EXECUTION_STEPS + "). Possible infinite loop detected.");
            }

            int currentIndex = context.getCurrentInstructionIndex();
            if (currentIndex < 0 || currentIndex >= instructions.size()) {
                context.terminate("Invalid instruction index: " + currentIndex);
                break;
            }

            SInstruction currentInstruction = instructions.get(currentIndex);
            context.addExecutedInstruction(currentInstruction);

            try {
                currentInstruction.execute(context);
            } catch (Exception e) {
                throw new ExecutionException("Error executing instruction at index " + currentIndex + 
                    " (" + currentInstruction.getName() + "): " + e.getMessage(), e);
            }

            handlePendingJump(context);
        }

        if (!context.isProgramTerminated()) {
            if (context.getCurrentInstructionIndex() >= instructions.size()) {
                context.terminate("Program completed - reached end of instructions");
            }
        }
    }

    private void handlePendingJump(ExecutionContext context) throws ExecutionException {
        String pendingJump = context.getPendingJumpLabel();
        if (pendingJump != null) {
            context.clearPendingJump();
            throw new ExecutionException("Jump to undefined label: " + pendingJump);
        }
    }
}
