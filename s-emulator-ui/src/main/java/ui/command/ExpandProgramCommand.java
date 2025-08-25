package ui.command;

import engine.api.SEmulatorEngine;
import ui.console.ConsoleInterface;

public class ExpandProgramCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            int maxLevel = engine.getMaxExpansionLevel();
            
            if (maxLevel == 0) {
                ui.displayInfo("This program contains only basic instructions and cannot be expanded.");
                ui.displayInfo("Showing original program:");
                String programDisplay = engine.displayProgram();
                ui.displayProgramOutput(programDisplay);
                return;
            }
            
            ui.displayInfo("Maximum expansion level for this program: " + maxLevel);
            int targetLevel = ui.getExpansionLevel(maxLevel);
            
            ui.displayInfo("Expanding program to level " + targetLevel + "...");
            
            String expandedProgram = engine.expandProgramWithHistory(targetLevel);
            ui.displayProgramOutput(expandedProgram);
            
        } catch (Exception e) {
            ui.displayError("Failed to expand program: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "Expand program to specified level";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return engine.isProgramLoaded();
    }
}
