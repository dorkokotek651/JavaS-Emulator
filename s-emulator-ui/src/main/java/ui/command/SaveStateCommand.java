package ui.command;

import engine.api.SEmulatorEngine;
import engine.exception.StateSerializationException;
import ui.console.ConsoleInterface;

public class SaveStateCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            ui.displayInfo("Save System State");
            ui.displayThinSeparator();
            
            String filePath = ui.getFilePathInput("Enter file path to save state (without .json extension): ");
            
            if (filePath == null || filePath.trim().isEmpty()) {
                ui.displayError("File path cannot be empty");
                return;
            }
            
            ui.displayInfo("Saving system state to: " + filePath + ".json");
            
            engine.saveState(filePath);
            
            ui.displaySuccess("System state saved successfully!");
            ui.displayInfo("File: " + filePath + ".json");
            
            if (engine.isProgramLoaded()) {
                ui.displayInfo("Saved program: " + engine.getCurrentProgram().getName());
                ui.displayInfo("Execution history: " + engine.getExecutionHistory().size() + " runs");
            } else {
                ui.displayInfo("No program was loaded - saved empty state");
            }
            
        } catch (StateSerializationException e) {
            ui.displayError("Failed to save system state: " + e.getMessage());
            
            Throwable cause = e.getCause();
            if (cause != null && !cause.getMessage().equals(e.getMessage())) {
                ui.displayError("Details: " + cause.getMessage());
            }
            
        } catch (Exception e) {
            ui.displayError("Unexpected error while saving state: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Save system state to file";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return true;
    }
}
