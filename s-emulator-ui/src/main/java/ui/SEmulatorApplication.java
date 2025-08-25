package ui;

import engine.api.SEmulatorEngine;
import engine.exception.SProgramException;
import engine.model.SEmulatorEngineImpl;
import ui.console.ConsoleInterface;
import ui.console.MenuManager;

public class SEmulatorApplication {
    
    public static void main(String[] args) {
        SEmulatorApplication app = new SEmulatorApplication();
        app.run();
    }
    
    public void run() {
        ConsoleInterface ui = null;
        SEmulatorEngine engine = null;
        
        try {
            // Initialize console interface
            ui = new ConsoleInterface();
            
            // Initialize S-Emulator engine
            ui.displayInfo("Initializing S-Emulator engine...");
            engine = new SEmulatorEngineImpl();
            ui.displaySuccess("S-Emulator engine initialized successfully!");
            
            // Create menu manager and run main loop
            MenuManager menuManager = new MenuManager(ui);
            menuManager.runMainLoop(engine);
            
        } catch (SProgramException e) {
            ui.displayError("Failed to initialize S-Emulator engine: " + e.getMessage());
            
            Throwable cause = e.getCause();
            if (cause != null) {
                ui.displayError("Details: " + cause.getMessage());
            }
            
            ui.displayError("The application cannot start. Please check your system configuration.");
            
        } catch (Exception e) {
            if (ui != null) {
                ui.displayError("Unexpected error occurred: " + e.getMessage());
                ui.displayError("The application will now exit.");
            } else {
                System.err.println("FATAL ERROR: " + e.getMessage());
                e.printStackTrace();
            }
            
        } finally {
            // Clean up resources
            if (ui != null) {
                try {
                    ui.close();
                } catch (Exception e) {
                    System.err.println("Error closing console interface: " + e.getMessage());
                }
            }
        }
    }
}
