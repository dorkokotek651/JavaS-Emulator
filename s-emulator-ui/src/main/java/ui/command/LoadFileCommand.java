package ui.command;

import engine.api.SEmulatorEngine;
import engine.exception.SProgramException;
import ui.console.ConsoleInterface;

public class LoadFileCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            String filePath = ui.getFilePath();
            
            ui.displayInfo("Loading program from: " + filePath);
            
            engine.loadProgram(filePath);
            
            ui.displaySuccess("Program loaded successfully!");
            ui.displayInfo("Program: " + engine.getCurrentProgram().getName());
            ui.displayInfo("Input variables: " + engine.getCurrentProgram().getInputVariables());
            ui.displayInfo("Max expansion level: " + engine.getMaxExpansionLevel());
            
        } catch (SProgramException e) {
            ui.displayError("Failed to load program: " + e.getMessage());
            
            // Show more detailed error information if available
            Throwable cause = e.getCause();
            if (cause != null && !cause.getMessage().equals(e.getMessage())) {
                ui.displayError("Details: " + cause.getMessage());
            }
            
        } catch (Exception e) {
            ui.displayError("Unexpected error while loading program: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Load S-program from XML file";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return true; // Always available
    }
}
