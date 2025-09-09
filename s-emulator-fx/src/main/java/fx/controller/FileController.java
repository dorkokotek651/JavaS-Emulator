package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.exception.SProgramException;
import fx.service.FileService;
import fx.util.ErrorDialogUtil;
import javafx.stage.Stage;
import java.io.File;
import java.util.function.Consumer;

/**
 * Controller responsible for file operations in the S-Emulator application.
 * Handles file loading, validation, and state management.
 */
public class FileController {
    
    private final SEmulatorEngine engine;
    private final FileService fileService;
    private Stage primaryStage;
    
    // Callbacks for communication with main controller
    private Consumer<String> statusUpdater;
    private Runnable onProgramLoaded;
    private Runnable onProgramStateCleared;
    
    /**
     * Creates a new FileController.
     * 
     * @param engine the S-Emulator engine instance
     * @param fileService the file service for file operations
     */
    public FileController(SEmulatorEngine engine, FileService fileService) {
        this.engine = engine;
        this.fileService = fileService;
    }
    
    /**
     * Sets the primary stage for file dialogs.
     * 
     * @param primaryStage the primary stage
     */
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    /**
     * Sets the status update callback.
     * 
     * @param statusUpdater callback to update status messages
     */
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    /**
     * Sets the program loaded callback.
     * 
     * @param onProgramLoaded callback when program is successfully loaded
     */
    public void setOnProgramLoaded(Runnable onProgramLoaded) {
        this.onProgramLoaded = onProgramLoaded;
    }
    
    /**
     * Sets the program state cleared callback.
     * 
     * @param onProgramStateCleared callback when program state should be cleared
     */
    public void setOnProgramStateCleared(Runnable onProgramStateCleared) {
        this.onProgramStateCleared = onProgramStateCleared;
    }
    
    /**
     * Handles file loading request from user.
     */
    public void handleLoadFile() {
        if (primaryStage == null) {
            updateStatus("Error: No primary stage available for file dialog");
            return;
        }
        
        File selectedFile = fileService.showLoadFileDialog(primaryStage);
        if (selectedFile != null) {
            updateStatus("Selected file: " + selectedFile.getName());
            loadProgramFileDirectly(selectedFile);
        }
    }
    
    /**
     * Loads a program file directly without progress dialog.
     * 
     * @param file the file to load
     */
    public void loadProgramFileDirectly(File file) {
        updateStatus("Loading file: " + file.getName());
        
        try {
            // Clear all previous program state before loading new program
            if (onProgramStateCleared != null) {
                onProgramStateCleared.run();
            }
            
            // Direct engine loading
            engine.loadProgram(file.getAbsolutePath());
            
            updateStatus("Engine loading completed");
            
            // Check if program was actually loaded
            if (engine.isProgramLoaded()) {
                SProgram program = engine.getCurrentProgram();
                updateStatus("Program loaded: " + program.getName() + " with " + program.getInstructions().size() + " instructions");
                
                // Notify that program was loaded successfully
                if (onProgramLoaded != null) {
                    onProgramLoaded.run();
                }
                
            } else {
                updateStatus("Error: Program not loaded in engine");
            }
            
        } catch (SProgramException e) {
            updateStatus("Error loading program: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "File Loading Failed", e.getMessage());
        } catch (Exception e) {
            updateStatus("Unexpected error: " + e.getMessage());
            ErrorDialogUtil.showError(primaryStage, "Unexpected Error", e.getMessage());
        }
    }
    
    /**
     * Loads a program file with progress indication.
     * 
     * @param file the file to load
     */
    public void loadProgramFileWithProgress(File file) {
        // Clear all previous program state before loading new program
        if (onProgramStateCleared != null) {
            onProgramStateCleared.run();
        }
        
        fileService.loadProgramFileWithProgress(
            file, 
            primaryStage,
            () -> {
                // Success callback
                System.out.println("File load success callback triggered");
                System.out.println("Engine program loaded: " + engine.isProgramLoaded());
                
                if (engine.isProgramLoaded()) {
                    SProgram program = engine.getCurrentProgram();
                    System.out.println("Program: " + (program != null ? program.getName() : "null"));
                    System.out.println("Instructions count: " + (program != null ? program.getInstructions().size() : "N/A"));
                }
                
                updateStatus("Program loaded successfully: " + file.getName());
                
                // Notify that program was loaded successfully
                if (onProgramLoaded != null) {
                    onProgramLoaded.run();
                }
            },
            (errorMessage) -> {
                // Error callback
                System.out.println("File load error callback triggered: " + errorMessage);
                ErrorDialogUtil.showError(primaryStage, "File Loading Failed", errorMessage);
                updateStatus("Failed to load file: " + file.getName());
            }
        );
    }
    
    /**
     * Handles save state request (placeholder for Phase 6).
     */
    public void handleSaveState() {
        updateStatus("Save state functionality will be implemented in Phase 6");
    }
    
    /**
     * Handles load state request (placeholder for Phase 6).
     */
    public void handleLoadState() {
        // When loading state is implemented, we should also clear previous state
        // if (onProgramStateCleared != null) {
        //     onProgramStateCleared.run();
        // }
        updateStatus("Load state functionality will be implemented in Phase 6");
    }
    
    /**
     * Updates status through the registered callback.
     * 
     * @param message the status message
     */
    private void updateStatus(String message) {
        if (statusUpdater != null) {
            statusUpdater.accept(message);
        }
        // Also print to console for debugging
        System.out.println("FileController Status: " + message);
    }
}
