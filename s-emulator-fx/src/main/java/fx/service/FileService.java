package fx.service;

import engine.api.SEmulatorEngine;
import engine.exception.SProgramException;
import fx.util.FileValidator;
import fx.util.ProgressDialog;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.function.Consumer;

/**
 * Service class for file operations in the S-Emulator JavaFX application.
 * Handles file loading with progress indication and validation.
 */
public class FileService {
    
    private final SEmulatorEngine engine;
    private final FileValidator validator;
    
    /**
     * Creates a new file service.
     * 
     * @param engine the S-Emulator engine instance
     */
    public FileService(SEmulatorEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine cannot be null");
        }
        this.engine = engine;
        this.validator = new FileValidator(engine);
    }
    
    /**
     * Loads a program file with progress indication and validation.
     * 
     * @param file the file to load
     * @param parentStage the parent stage for dialogs
     * @param onSuccess callback for successful loading
     * @param onError callback for loading errors
     */
    public void loadProgramFileWithProgress(File file, Stage parentStage, 
                                          Runnable onSuccess, Consumer<String> onError) {
        if (file == null) {
            if (onError != null) {
                onError.accept("No file selected");
            }
            return;
        }
        
        // Pre-validate file
        try {
            validateFile(file);
        } catch (IllegalArgumentException e) {
            if (onError != null) {
                onError.accept(e.getMessage());
            }
            return;
        }
        
        // Create and show progress dialog
        ProgressDialog progressDialog = new ProgressDialog(parentStage);
        Task<Void> loadTask = createLoadProgramTask(file);
        
        // Handle task completion
        loadTask.setOnSucceeded(e -> {
            if (onSuccess != null) {
                onSuccess.run();
            }
        });
        
        loadTask.setOnFailed(e -> {
            Throwable exception = loadTask.getException();
            String errorMessage = getFriendlyErrorMessage(exception instanceof Exception ? (Exception) exception : new Exception(exception));
            if (onError != null) {
                onError.accept(errorMessage);
            }
        });
        
        progressDialog.showAndWait(loadTask, "Loading Program File");
    }
    
    /**
     * Shows a file chooser dialog for selecting XML program files.
     * 
     * @param parentStage the parent stage for the dialog
     * @return the selected file, or null if cancelled
     */
    public File showLoadFileDialog(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load S-Program File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        
        // Set initial directory to user home if available
        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            File initialDir = new File(userHome);
            if (initialDir.exists() && initialDir.isDirectory()) {
                fileChooser.setInitialDirectory(initialDir);
            }
        }
        
        return fileChooser.showOpenDialog(parentStage);
    }
    
    /**
     * Creates a task for loading a program file with progress indication.
     * 
     * @param file the file to load
     * @return a JavaFX Task that loads the program
     */
    public Task<Void> createLoadProgramTask(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        return new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Loading Program File");
                updateMessage("Validating file: " + file.getName());
                updateProgress(0, 100);
                
                // Simulate progress with artificial delay
                Thread.sleep(500);
                updateProgress(25, 100);
                
                updateMessage("Parsing XML structure...");
                Thread.sleep(300);
                updateProgress(50, 100);
                
                updateMessage("Validating program structure...");
                Thread.sleep(400);
                updateProgress(75, 100);
                
                // Actually load the program
                updateMessage("Loading program into engine...");
                try {
                    engine.loadProgram(file.getAbsolutePath());
                } catch (SProgramException e) {
                    // Re-throw as RuntimeException to be handled by Task framework
                    throw new RuntimeException("Failed to load program: " + e.getMessage(), e);
                }
                
                updateProgress(100, 100);
                updateMessage("Program loaded successfully");
                Thread.sleep(200); // Brief pause to show completion
                
                return null;
            }
            
            @Override
            protected void succeeded() {
                updateMessage("File loaded successfully: " + file.getName());
            }
            
            @Override
            protected void failed() {
                Throwable exception = getException();
                if (exception != null) {
                    updateMessage("Failed to load file: " + exception.getMessage());
                } else {
                    updateMessage("Failed to load file: Unknown error");
                }
            }
            
            @Override
            protected void cancelled() {
                updateMessage("File loading cancelled");
            }
        };
    }
    
    /**
     * Validates that a file exists and is readable.
     * 
     * @param file the file to validate
     * @throws IllegalArgumentException if file is invalid
     */
    public void validateFile(File file) {
        if (file == null) {
            throw new IllegalArgumentException("File cannot be null");
        }
        
        if (!file.exists()) {
            throw new IllegalArgumentException("File does not exist: " + file.getAbsolutePath());
        }
        
        if (!file.isFile()) {
            throw new IllegalArgumentException("Path is not a file: " + file.getAbsolutePath());
        }
        
        if (!file.canRead()) {
            throw new IllegalArgumentException("File is not readable: " + file.getAbsolutePath());
        }
        
        // Check file extension
        String fileName = file.getName().toLowerCase();
        if (!fileName.endsWith(".xml")) {
            throw new IllegalArgumentException("File must be an XML file: " + file.getName());
        }
        
        // Check file size (reasonable limit)
        long fileSizeKB = file.length() / 1024;
        if (fileSizeKB > 10240) { // 10MB limit
            throw new IllegalArgumentException("File is too large (max 10MB): " + fileSizeKB + "KB");
        }
    }
    
    /**
     * Checks if a file path contains spaces and handles it properly.
     * 
     * @param file the file to check
     * @return true if file path contains spaces, false otherwise
     */
    public boolean hasSpacesInPath(File file) {
        if (file == null) {
            return false;
        }
        
        return file.getAbsolutePath().contains(" ");
    }
    
    /**
     * Gets a user-friendly error message for common file loading errors.
     * 
     * @param exception the exception that occurred
     * @return a user-friendly error message
     */
    public String getFriendlyErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }
        
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = exception.getClass().getSimpleName();
        }
        
        // Convert technical error messages to user-friendly ones
        if (message.contains("XML") && message.contains("validation")) {
            return "The XML file format is invalid or corrupted. Please check the file structure.";
        } else if (message.contains("FileNotFoundException")) {
            return "The selected file could not be found. It may have been moved or deleted.";
        } else if (message.contains("AccessDeniedException")) {
            return "Permission denied. Please check that you have read access to the file.";
        } else if (message.contains("function") && message.contains("not defined")) {
            return "The program references functions that are not defined. Please check function definitions.";
        } else if (message.contains("label") && message.contains("not defined")) {
            return "The program references labels that are not defined. Please check label definitions.";
        } else {
            return message;
        }
    }
}
