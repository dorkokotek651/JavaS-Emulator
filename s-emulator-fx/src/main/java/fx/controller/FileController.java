package fx.controller;

import engine.api.SEmulatorEngine;
import engine.api.SProgram;
import engine.exception.SProgramException;
import fx.service.FileService;
import fx.util.ErrorDialogUtil;
import javafx.stage.Stage;
import java.io.File;
import java.util.function.Consumer;

public class FileController {
    
    private final SEmulatorEngine engine;
    private final FileService fileService;
    private Stage primaryStage;

    private Consumer<String> statusUpdater;
    private Runnable onProgramLoaded;
    private Runnable onProgramStateCleared;
    
    public FileController(SEmulatorEngine engine, FileService fileService) {
        this.engine = engine;
        this.fileService = fileService;
    }
    
    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
    
    public void setStatusUpdater(Consumer<String> statusUpdater) {
        this.statusUpdater = statusUpdater;
    }
    
    public void setOnProgramLoaded(Runnable onProgramLoaded) {
        this.onProgramLoaded = onProgramLoaded;
    }
    
    public void setOnProgramStateCleared(Runnable onProgramStateCleared) {
        this.onProgramStateCleared = onProgramStateCleared;
    }
    
    public void handleLoadFile() {
        if (primaryStage == null) {
            updateStatus("Error: No primary stage available for file dialog");
            return;
        }
        
        File selectedFile = fileService.showLoadFileDialog(primaryStage);
        if (selectedFile != null) {
            updateStatus("Selected file: " + selectedFile.getName());
            loadProgramFileWithProgress(selectedFile);
        }
    }
    
    public void loadProgramFileDirectly(File file) {
        updateStatus("Loading file: " + file.getName());
        
        try {

            if (onProgramStateCleared != null) {
                onProgramStateCleared.run();
            }

            engine.loadProgram(file.getAbsolutePath());
            
            updateStatus("Engine loading completed");

            if (engine.isProgramLoaded()) {
                SProgram program = engine.getCurrentProgram();
                updateStatus("Program loaded: " + program.getName() + " with " + program.getInstructions().size() + " instructions");

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
    
    public void loadProgramFileWithProgress(File file) {

        if (onProgramStateCleared != null) {
            onProgramStateCleared.run();
        }
        
        fileService.loadProgramFileWithProgress(
            file, 
            primaryStage,
            () -> {

                if (engine.isProgramLoaded()) {
                    SProgram program = engine.getCurrentProgram();
                }
                
                updateStatus("Program loaded successfully: " + file.getName());

                if (onProgramLoaded != null) {
                    onProgramLoaded.run();
                } else {
                }
            },
            (errorMessage) -> {

                ErrorDialogUtil.showError(primaryStage, "File Loading Failed", errorMessage);
                updateStatus("Failed to load file: " + file.getName());
            }
        );
    }

    private void updateStatus(String message) {
        if (statusUpdater != null) {
            statusUpdater.accept(message);
        }

    }
}
