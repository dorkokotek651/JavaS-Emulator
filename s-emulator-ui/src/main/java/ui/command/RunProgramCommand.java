package ui.command;

import engine.api.ExecutionResult;
import engine.api.SEmulatorEngine;
import ui.console.ConsoleInterface;
import ui.console.OutputFormatter;
import java.util.List;

public class RunProgramCommand implements Command {

    private String extractInstructionsFromProgram(String expandedProgram) {
        if (expandedProgram == null || expandedProgram.trim().isEmpty()) {
            return "No instructions found.";
        }
        
        String[] lines = expandedProgram.split("\n");
        StringBuilder instructionsOnly = new StringBuilder();
        boolean inInstructionsSection = false;
        
        for (String line : lines) {
            if (line.startsWith("Instructions:")) {
                inInstructionsSection = true;
                continue;
            }
            
            if (inInstructionsSection && line.trim().startsWith("#")) {
                instructionsOnly.append(line).append("\n");
            }
        }
        
        return instructionsOnly.toString().trim();
    }

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            int maxLevel = engine.getMaxExpansionLevel();
            int expansionLevel;
            
            if (maxLevel == 0) {
                ui.displayInfo("This program contains only basic instructions (expansion level 0).");
                expansionLevel = 0;
            } else {
                ui.displayInfo("Maximum expansion level for this program: " + maxLevel);
                expansionLevel = ui.getExpansionLevel(maxLevel);
            }
            
            List<String> inputVariables = engine.getCurrentProgram().getInputVariables();
            List<Integer> inputs = ui.getProgramInputs(inputVariables);
            
            String confirmMessage = String.format(
                "Execute program at expansion level %d with inputs %s?", 
                expansionLevel, inputs
            );
            
            if (!ui.confirmAction(confirmMessage)) {
                ui.displayInfo("Program execution cancelled.");
                return;
            }
            
            ui.displayInfo("Executing program...");
            
            ExecutionResult result = engine.runProgram(expansionLevel, inputs);
            
            ui.displaySuccess("Program executed successfully!");
            ui.displaySeparator();
            
            // Step 4: Show the actual running program
            String expandedProgram = engine.expandProgramWithHistory(expansionLevel);
            String instructionsOnly = extractInstructionsFromProgram(expandedProgram);
            System.out.println("Program Executed:");
            System.out.println(instructionsOnly);
            ui.displaySeparator();
            
            // Step 5: Show the value of y (the formal result)
            System.out.println("Result (y): " + result.getYValue());
            
            // Step 6: Show the other variables
            System.out.println("Variable Values: " + OutputFormatter.formatVariableValues(
                result.getYValue(), result.getInputVariables(), result.getWorkingVariables()));
            
            // Step 7: Show the number of cycles
            System.out.println("Total Cycles: " + result.getTotalCycles());
            ui.displaySeparator();
            
            ui.displayInfo("Execution completed in " + result.getTotalCycles() + " cycles.");
            ui.displayInfo("Final result (y): " + result.getYValue());
            
        } catch (IllegalArgumentException e) {
            ui.displayError("Invalid input: " + e.getMessage());
        } catch (RuntimeException e) {
            ui.displayError("Execution failed: " + e.getMessage());
            
            Throwable cause = e.getCause();
            if (cause != null && !cause.getMessage().equals(e.getMessage())) {
                ui.displayError("Details: " + cause.getMessage());
            }
            
        } catch (Exception e) {
            ui.displayError("Unexpected error during execution: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Run program with specified inputs";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return engine.isProgramLoaded();
    }
}
