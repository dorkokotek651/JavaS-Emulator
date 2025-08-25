package ui.command;

import engine.api.SEmulatorEngine;
import ui.console.ConsoleInterface;

public class ViewProgramCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            String programDisplay = engine.displayProgram();
            ui.displayProgramOutput(programDisplay);
            
        } catch (Exception e) {
            ui.displayError("Failed to display program: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "View current program details";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return engine.isProgramLoaded();
    }
}
