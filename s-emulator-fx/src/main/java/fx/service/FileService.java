package fx.service;

import engine.api.SEmulatorEngine;
import engine.exception.SProgramException;
import fx.util.FileLoadingProgressDialog;
import javafx.concurrent.Task;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.function.Consumer;

public class FileService {
    
    private final SEmulatorEngine engine;
    
    public FileService(SEmulatorEngine engine) {
        if (engine == null) {
            throw new IllegalArgumentException("Engine cannot be null");
        }
        this.engine = engine;
    }
    
    public void loadProgramFileWithProgress(File file, Stage parentStage, 
                                          Runnable onSuccess, Consumer<String> onError) {
        if (file == null) {
            if (onError != null) {
                onError.accept("No file selected");
            }
            return;
        }
        

        try {
            validateFile(file);
        } catch (IllegalArgumentException e) {
            if (onError != null) {
                onError.accept(e.getMessage());
            }
            return;
        }
        

        FileLoadingProgressDialog progressDialog = new FileLoadingProgressDialog(parentStage);
        
        System.out.println("FileService: Creating task with event handlers");
        
        // Create task and set up event handlers before starting
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                updateTitle("Loading Program File");
                updateMessage("Validating file: " + file.getName());
                updateProgress(0, 100);
                
                Thread.sleep(500);
                updateProgress(25, 100);
                
                updateMessage("Parsing XML structure...");
                Thread.sleep(300);
                updateProgress(50, 100);
                
                updateMessage("Validating program structure...");
                Thread.sleep(400);
                updateProgress(75, 100);
                
                updateMessage("Loading program into engine...");
                try {
                    System.out.println("FileService Task: About to call engine.loadProgram");
                    engine.loadProgram(file.getAbsolutePath());
                    System.out.println("FileService Task: engine.loadProgram completed successfully");
                } catch (SProgramException e) {
                    System.out.println("FileService Task: SProgramException caught: " + e.getMessage());
                    throw new RuntimeException("Failed to load program: " + e.getMessage(), e);
                } catch (Exception e) {
                    System.out.println("FileService Task: Unexpected exception caught: " + e.getMessage());
                    throw e;
                }
                
                updateProgress(100, 100);
                updateMessage("Program loaded successfully");
                System.out.println("FileService Task: About to sleep and return");
                Thread.sleep(200);
                System.out.println("FileService Task: Returning null - task should succeed");
                
                updateMessage("Task completed successfully");
                
                return null;
            }
        };
        
        // Set up event handlers
        loadTask.setOnSucceeded(e -> {
            System.out.println("FileService: Task succeeded, executing success callback");
            javafx.application.Platform.runLater(() -> {
                if (onSuccess != null) {
                    System.out.println("FileService: Executing onSuccess callback");
                    onSuccess.run();
                } else {
                    System.out.println("FileService: onSuccess callback is null");
                }
            });
        });
        
        loadTask.setOnFailed(e -> {
            System.out.println("FileService: Task failed, executing error callback");
            javafx.application.Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = getFriendlyErrorMessage(exception instanceof Exception ? (Exception) exception : new Exception(exception));
                if (onError != null) {
                    onError.accept(errorMessage);
                }
            });
        });
        
        // Add a listener to track task state changes
        loadTask.stateProperty().addListener((obs, oldState, newState) -> {
            System.out.println("FileService: Task state changed from " + oldState + " to " + newState);
        });
        
        System.out.println("FileService: Event handlers set up, starting task");
        
        // Start the task
        Thread taskThread = new Thread(loadTask);
        taskThread.setDaemon(true);
        taskThread.start();
        
        System.out.println("FileService: Task started, showing dialog");
        
        progressDialog.showAndWait(loadTask, file.getName());
        
        // Manual check after dialog closes - this is a fallback
        System.out.println("FileService: Dialog closed, checking task state manually");
        if (loadTask.getState() == javafx.concurrent.Worker.State.SUCCEEDED) {
            System.out.println("FileService: Task succeeded (manual check), executing success callback");
            if (onSuccess != null) {
                System.out.println("FileService: Executing onSuccess callback (manual)");
                onSuccess.run();
            } else {
                System.out.println("FileService: onSuccess callback is null (manual)");
            }
        } else if (loadTask.getState() == javafx.concurrent.Worker.State.FAILED) {
            System.out.println("FileService: Task failed (manual check), executing error callback");
            Throwable exception = loadTask.getException();
            String errorMessage = getFriendlyErrorMessage(exception instanceof Exception ? (Exception) exception : new Exception(exception));
            if (onError != null) {
                onError.accept(errorMessage);
            }
        } else {
            System.out.println("FileService: Task state is: " + loadTask.getState());
        }
    }
    
    public File showLoadFileDialog(Stage parentStage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Load S-Program File");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        

        String userHome = System.getProperty("user.home");
        if (userHome != null) {
            File initialDir = new File(userHome);
            if (initialDir.exists() && initialDir.isDirectory()) {
                fileChooser.setInitialDirectory(initialDir);
            }
        }
        
        return fileChooser.showOpenDialog(parentStage);
    }
    
    
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
        

        String fileName = file.getName().toLowerCase(java.util.Locale.ENGLISH);
        if (!fileName.endsWith(".xml")) {
            throw new IllegalArgumentException("File must be an XML file: " + file.getName());
        }
        

        long fileSizeKB = file.length() / 1024;
        if (fileSizeKB > 10240) {
            throw new IllegalArgumentException("File is too large (max 10MB): " + fileSizeKB + "KB");
        }
    }
    
    public boolean hasSpacesInPath(File file) {
        if (file == null) {
            return false;
        }
        
        return file.getAbsolutePath().contains(" ");
    }
    
    public String getFriendlyErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }
        
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = exception.getClass().getSimpleName();
        }
        

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
