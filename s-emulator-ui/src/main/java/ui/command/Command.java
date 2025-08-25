package ui.command;

import engine.api.SEmulatorEngine;
import ui.console.ConsoleInterface;

public interface Command {
    
    void execute(ConsoleInterface ui, SEmulatorEngine engine);
    
    String getDescription();
    
    boolean isAvailable(SEmulatorEngine engine);
    
    default String getName() {
        return this.getClass().getSimpleName().replace("Command", "");
    }
}
