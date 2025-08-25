package ui.console;

import engine.api.SEmulatorEngine;
import ui.command.Command;
import ui.command.LoadFileCommand;
import ui.command.ViewProgramCommand;
import ui.command.ExpandProgramCommand;
import ui.command.RunProgramCommand;
import ui.command.ViewHistoryCommand;
import ui.command.ExitCommand;
import java.util.ArrayList;
import java.util.List;

public class MenuManager {
    private final List<Command> allCommands;
    private final ConsoleInterface ui;

    public MenuManager(ConsoleInterface ui) {
        this.ui = ui;
        this.allCommands = initializeCommands();
    }

    private List<Command> initializeCommands() {
        List<Command> commands = new ArrayList<>();
        
        commands.add(new LoadFileCommand());
        commands.add(new ViewProgramCommand());
        commands.add(new ExpandProgramCommand());
        commands.add(new RunProgramCommand());
        commands.add(new ViewHistoryCommand());
        commands.add(new ExitCommand());
        
        return commands;
    }

    public void displayMenu(SEmulatorEngine engine) {
        List<Command> availableCommands = getAvailableCommands(engine);
        List<String> menuOptions = new ArrayList<>();
        
        for (Command command : availableCommands) {
            menuOptions.add(command.getDescription());
        }
        
        String title = "S-Emulator Main Menu";
        if (engine.isProgramLoaded()) {
            title += " - Program: " + engine.getCurrentProgram().getName();
        }
        
        ui.displayMenu(title, menuOptions);
    }

    public void handleUserChoice(int choice, SEmulatorEngine engine) {
        List<Command> availableCommands = getAvailableCommands(engine);
        
        if (choice < 1 || choice > availableCommands.size()) {
            ui.displayError("Invalid choice: " + choice);
            return;
        }
        
        Command selectedCommand = availableCommands.get(choice - 1);
        
        try {
            ui.displayInfo("Executing: " + selectedCommand.getDescription());
            ui.displayThinSeparator();
            
            selectedCommand.execute(ui, engine);
            
        } catch (Exception e) {
            ui.displayError("Command execution failed: " + e.getMessage());
            
            // Show stack trace for debugging if needed
            if (e.getCause() != null) {
                ui.displayError("Caused by: " + e.getCause().getMessage());
            }
        }
        
        // Pause before returning to menu (except for exit command)
        if (!(selectedCommand instanceof ExitCommand) && ui.isRunning()) {
            ui.displayThinSeparator();
            ui.waitForEnter();
        }
    }

    public List<Command> getAvailableCommands(SEmulatorEngine engine) {
        List<Command> availableCommands = new ArrayList<>();
        
        for (Command command : allCommands) {
            if (command.isAvailable(engine)) {
                availableCommands.add(command);
            }
        }
        
        return availableCommands;
    }

    public int getMenuSize(SEmulatorEngine engine) {
        return getAvailableCommands(engine).size();
    }

    public void displayProgramStatus(SEmulatorEngine engine) {
        ui.displayThinSeparator();
        
        if (engine.isProgramLoaded()) {
            ui.displayInfo("Current program: " + engine.getCurrentProgram().getName());
            ui.displayInfo("Input variables: " + engine.getCurrentProgram().getInputVariables());
            ui.displayInfo("Execution history: " + engine.getExecutionHistory().size() + " runs");
        } else {
            ui.displayInfo("No program loaded. Please load a program first.");
        }
        
        ui.displayThinSeparator();
    }

    public void runMainLoop(SEmulatorEngine engine) {
        ui.displayWelcome();
        
        while (ui.isRunning()) {
            try {
                displayProgramStatus(engine);
                displayMenu(engine);
                
                int menuSize = getMenuSize(engine);
                int choice = ui.getUserChoice(1, menuSize);
                
                handleUserChoice(choice, engine);
                
            } catch (Exception e) {
                ui.displayError("Unexpected error in main loop: " + e.getMessage());
                ui.displayInfo("Continuing...");
                ui.waitForEnter();
            }
        }
        
        ui.displayGoodbye();
        ui.close();
    }
}
