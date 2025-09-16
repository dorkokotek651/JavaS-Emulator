package fx.service;

import engine.api.SEmulatorEngine;
import engine.exception.SProgramException;
import fx.util.ProgressDialog;
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
        

        ProgressDialog progressDialog = new ProgressDialog(parentStage);
        Task<Void> loadTask = createLoadProgramTask(file);
        

        loadTask.setOnSucceeded(e -> {

            javafx.application.Platform.runLater(() -> {
                if (onSuccess != null) {
                    onSuccess.run();
                }
            });
        });
        
        loadTask.setOnFailed(e -> {

            javafx.application.Platform.runLater(() -> {
                Throwable exception = loadTask.getException();
                String errorMessage = getFriendlyErrorMessage(exception instanceof Exception ? (Exception) exception : new Exception(exception));
                if (onError != null) {
                    onError.accept(errorMessage);
                }
            });
        });
        
        progressDialog.showAndWait(loadTask, "Loading Program File");
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
                    engine.loadProgram(file.getAbsolutePath());
                } catch (SProgramException e) {

                    throw new RuntimeException("Failed to load program: " + e.getMessage(), e);
                }
                
                updateProgress(100, 100);
                updateMessage("Program loaded successfully");
                Thread.sleep(200);
                
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
