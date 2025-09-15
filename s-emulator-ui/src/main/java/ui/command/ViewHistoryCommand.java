package ui.command;

import engine.api.ExecutionResult;
import engine.api.SEmulatorEngine;
import ui.console.ConsoleInterface;
import ui.console.OutputFormatter;
import java.util.List;

public class ViewHistoryCommand implements Command {

    @Override
    public void execute(ConsoleInterface ui, SEmulatorEngine engine) {
        try {
            List<ExecutionResult> history = engine.getExecutionHistory();
            
            if (history.isEmpty()) {
                ui.displayInfo("No execution history available. Run the program first to see execution results.");
                return;
            }
            
            ui.displayInfo("Found " + history.size() + " execution(s) in history.");
            
            ui.displaySeparator();
            System.out.println(OutputFormatter.formatExecutionHistory(history));
            ui.displaySeparator();
            


                




                            




                            





                            






            
        } catch (Exception e) {
            ui.displayError("Failed to display execution history: " + e.getMessage());
        }
    }

    @Override
    public String getDescription() {
        return "View execution history";
    }

    @Override
    public boolean isAvailable(SEmulatorEngine engine) {
        return engine.isProgramLoaded();
    }
}
