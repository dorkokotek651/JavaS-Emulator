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
            
            if (history.size() > 0) {
                boolean viewDetails = ui.confirmAction("Would you like to view detailed results for a specific execution?");
                
                if (viewDetails) {
                    while (true) {
                        try {
                            int runNumber = ui.getIntegerInput("Enter run number (1-" + history.size() + "): ");
                            
                            if (runNumber < 1 || runNumber > history.size()) {
                                ui.displayError("Invalid run number. Please enter a value between 1 and " + history.size() + ".");
                                continue;
                            }
                            
                            ExecutionResult selectedResult = history.get(runNumber - 1);
                            ui.displaySeparator();
                            System.out.println("Detailed view of " + OutputFormatter.formatExecutionResult(selectedResult));
                            ui.displaySeparator();
                            break;
                            
                        } catch (Exception e) {
                            ui.displayError("Invalid input: " + e.getMessage());
                        }
                    }
                }
            }
            
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
