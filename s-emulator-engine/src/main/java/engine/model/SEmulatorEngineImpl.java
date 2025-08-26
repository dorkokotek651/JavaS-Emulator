package engine.model;

import engine.api.ExecutionResult;
import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.exception.SProgramException;
import engine.exception.XMLValidationException;
import engine.exception.ExecutionException;
import engine.exception.ExpansionException;
import engine.execution.ProgramRunner;
import engine.expansion.ExpansionEngine;
import engine.xml.SProgramParser;
import java.util.ArrayList;
import java.util.List;

public class SEmulatorEngineImpl implements SEmulatorEngine {
    private SProgram currentProgram;
    private final List<ExecutionResult> executionHistory;
    private final SProgramParser parser;
    private final ProgramRunner runner;
    private final ExpansionEngine expansionEngine;
    private final engine.expansion.MultiLevelExpansionEngine multiLevelExpansionEngine;
    private int nextRunNumber;

    public SEmulatorEngineImpl() throws SProgramException {
        this.currentProgram = null;
        this.executionHistory = new ArrayList<>();
        this.nextRunNumber = 1;
        
        try {
            this.parser = new SProgramParser();
            this.runner = new ProgramRunner();
            this.expansionEngine = new ExpansionEngine();
            this.multiLevelExpansionEngine = new engine.expansion.MultiLevelExpansionEngine();
        } catch (XMLValidationException e) {
            throw new SProgramException("Failed to initialize S-Emulator engine", e);
        }
    }

    @Override
    public void loadProgram(String xmlFilePath) throws SProgramException {
        if (xmlFilePath == null || xmlFilePath.trim().isEmpty()) {
            throw new SProgramException("XML file path cannot be null or empty");
        }

        try {
            SProgram program = parser.parseXMLFile(xmlFilePath.trim());
            this.currentProgram = program;
            this.executionHistory.clear();
            this.nextRunNumber = 1;
        } catch (XMLValidationException e) {
            throw new SProgramException("Failed to load program from '" + xmlFilePath + "'", e);
        }
    }

    @Override
    public SProgram getCurrentProgram() {
        return currentProgram;
    }

    @Override
    public boolean isProgramLoaded() {
        return currentProgram != null;
    }

    @Override
    public String displayProgram() {
        if (!isProgramLoaded()) {
            return "No program loaded.";
        }

        StringBuilder display = new StringBuilder();
        display.append("Program: ").append(currentProgram.getName()).append("\n");
        display.append("Input Variables: ").append(currentProgram.getInputVariables()).append("\n");
        display.append("Labels: ").append(currentProgram.getLabels()).append("\n");
        display.append("Max Expansion Level: ").append(currentProgram.getMaxExpansionLevel()).append("\n");
        display.append("Instructions:\n");

        List<engine.api.SInstruction> instructions = currentProgram.getInstructions();
        for (int i = 0; i < instructions.size(); i++) {
            engine.api.SInstruction instruction = instructions.get(i);
            display.append(String.format("#%d %s\n", i + 1, instruction.toString()));
        }

        return display.toString();
    }

    @Override
    public String expandProgram(int level) {
        if (!isProgramLoaded()) {
            return "No program loaded.";
        }

        if (level < 0) {
            return "Expansion level cannot be negative.";
        }

        if (level > currentProgram.getMaxExpansionLevel()) {
            return "Expansion level " + level + " exceeds maximum level " + currentProgram.getMaxExpansionLevel() + ".";
        }

        if (level == 0) {
            return displayProgram();
        }

        try {
            SProgram expandedProgram = expansionEngine.expandProgram(currentProgram, level);
            
            StringBuilder display = new StringBuilder();
            display.append("Program: ").append(expandedProgram.getName()).append("\n");
            display.append("Expansion Level: ").append(level).append("\n");
            display.append("Input Variables: ").append(expandedProgram.getInputVariables()).append("\n");
            display.append("Labels: ").append(expandedProgram.getLabels()).append("\n");
            display.append("Instructions:\n");

            List<engine.api.SInstruction> instructions = expandedProgram.getInstructions();
            for (int i = 0; i < instructions.size(); i++) {
                engine.api.SInstruction instruction = instructions.get(i);
                display.append(String.format("#%-3d %s\n", i + 1, instruction.toStringWithHistory(i + 1)));
            }

            return display.toString();
            
        } catch (ExpansionException e) {
            return "Expansion failed: " + e.getMessage();
        }
    }

    @Override
    public String expandProgramWithHistory(int level) {
        if (!isProgramLoaded()) {
            return "No program loaded.";
        }

        if (level < 0) {
            return "Expansion level cannot be negative.";
        }

        if (level > currentProgram.getMaxExpansionLevel()) {
            return "Expansion level " + level + " exceeds maximum level " + currentProgram.getMaxExpansionLevel() + ".";
        }

        if (level == 0) {
            return displayProgram();
        }

        try {
            engine.expansion.MultiLevelExpansion multiLevel = multiLevelExpansionEngine.expandProgramToAllLevels(currentProgram);
            SProgram targetProgram = multiLevel.getLevel(level);
            
            if (targetProgram == null) {
                return "Failed to expand program to level " + level;
            }
            
            StringBuilder display = new StringBuilder();
            display.append("Program: ").append(targetProgram.getName()).append("\n");
            display.append("Expansion Level: ").append(level).append("\n");
            display.append("Input Variables: ").append(targetProgram.getInputVariables()).append("\n");
            display.append("Labels: ").append(targetProgram.getLabels()).append("\n");
            display.append("Instructions:\n");

            List<engine.api.SInstruction> instructions = targetProgram.getInstructions();
            for (int i = 0; i < instructions.size(); i++) {
                engine.api.SInstruction instruction = instructions.get(i);
                List<engine.expansion.MultiLevelExpansion.InstructionAncestor> ancestry = 
                    multiLevel.getInstructionAncestry(level, i);
                display.append(String.format("#%-3d %s\n", i + 1, instruction.toStringWithMultiLevelHistory(ancestry)));
            }

            return display.toString();
            
        } catch (engine.exception.ExpansionException e) {
            return "Expansion failed: " + e.getMessage();
        }
    }

    @Override
    public ExecutionResult runProgram(int expansionLevel, List<Integer> inputs) {
        if (!isProgramLoaded()) {
            throw new RuntimeException("No program loaded");
        }

        if (inputs == null) {
            throw new IllegalArgumentException("Inputs cannot be null");
        }

        if (expansionLevel < 0) {
            throw new IllegalArgumentException("Expansion level cannot be negative: " + expansionLevel);
        }

        if (expansionLevel > currentProgram.getMaxExpansionLevel()) {
            throw new IllegalArgumentException("Expansion level " + expansionLevel + 
                " exceeds maximum level " + currentProgram.getMaxExpansionLevel());
        }

        List<String> requiredInputs = currentProgram.getInputVariables();
        if (inputs.size() > requiredInputs.size()) {
            inputs = inputs.subList(0, requiredInputs.size());
        } else if (inputs.size() < requiredInputs.size()) {
            List<Integer> paddedInputs = new ArrayList<>(inputs);
            while (paddedInputs.size() < requiredInputs.size()) {
                paddedInputs.add(0);
            }
            inputs = paddedInputs;
        }

        for (int input : inputs) {
            if (input < 0) {
                throw new IllegalArgumentException("Input values cannot be negative: " + input);
            }
        }

        try {
            SProgram programToRun;
            if (expansionLevel == 0) {
                programToRun = currentProgram;
            } else {
                programToRun = expansionEngine.expandProgram(currentProgram, expansionLevel);
            }
            
            ExecutionResult result = runner.executeProgram(programToRun, inputs, nextRunNumber, expansionLevel);
            executionHistory.add(result);
            nextRunNumber++;
            return result;
        } catch (ExecutionException | ExpansionException e) {
            throw new RuntimeException("Program execution failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ExecutionResult> getExecutionHistory() {
        return List.copyOf(executionHistory);
    }

    @Override
    public int getMaxExpansionLevel() {
        if (!isProgramLoaded()) {
            return 0;
        }
        return currentProgram.getMaxExpansionLevel();
    }

    public void clearExecutionHistory() {
        executionHistory.clear();
        nextRunNumber = 1;
    }

    public int getExecutionCount() {
        return executionHistory.size();
    }
}
