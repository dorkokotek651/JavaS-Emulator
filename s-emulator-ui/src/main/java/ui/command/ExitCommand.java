package ui.command;

import engine.api.SEmulatorEngine;
import ui.console.ConsoleInterface;

public class ExitCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        boolean confirmExit = ui.confirmAction("Are you sure you want to exit?");
        
        if (confirmExit) {
            ui.displayInfo("Shutting down S-Emulator...");
            ui.setRunning(false);
        } else {
            ui.displayInfo("Exit cancelled. Returning to main menu.");
        }
    }

    @Override
    public String getDescription() {
        return "Exit the application";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return true;
    }
}
