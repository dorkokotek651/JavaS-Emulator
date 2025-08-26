package ui.command;

import engine.api.SEmulatorEngine;
import engine.exception.StateSerializationException;
import ui.console.ConsoleInterface;

public class LoadStateCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            ui.displayInfo("Load System State");
            ui.displayThinSeparator();
            
            String filePath = ui.getFilePathInput("Enter file path to load state (without .json extension): ");
            
            if (filePath == null || filePath.trim().isEmpty()) {
                ui.displayError("File path cannot be empty");
                return;
            }
            
            ui.displayInfo("Loading system state from: " + filePath + ".json");
            
            engine.loadState(filePath);
            
            ui.displaySuccess("System state loaded successfully!");
            ui.displayInfo("File: " + filePath + ".json");
            ui.displayThinSeparator();
            
            if (engine.isProgramLoaded()) {
                ui.displayInfo("Loaded program: " + engine.getCurrentProgram().getName());
                ui.displayInfo("Input variables: " + engine.getCurrentProgram().getInputVariables());
                ui.displayInfo("Max expansion level: " + engine.getMaxExpansionLevel());
                ui.displayInfo("Execution history: " + engine.getExecutionHistory().size() + " runs");
            } else {
                ui.displayInfo("No program in loaded state");
            }
            
        } catch (StateSerializationException e) {
            ui.displayError("Failed to load system state: " + e.getMessage());
            
            Throwable cause = e.getCause();
            if (cause != null && !cause.getMessage().equals(e.getMessage())) {
                ui.displayError("Details: " + cause.getMessage());
            }
            
        } catch (Exception e) {
            ui.displayError("Unexpected error while loading state: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Load system state from file";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return true;
    }
}
