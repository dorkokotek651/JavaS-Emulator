package fx.util;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * Utility class for showing progress dialogs during long-running operations.
 * Provides a modal dialog with progress bar and cancel functionality.
 */
public class ProgressDialog {
    
    private final Stage dialogStage;
    private final ProgressBar progressBar;
    private final Label messageLabel;
    private final Label titleLabel;
    private final Button cancelButton;
    private Task<?> currentTask;
    
    /**
     * Creates a new progress dialog.
     * 
     * @param owner the owner stage
     */
    public ProgressDialog(Stage owner) {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setResizable(false);
        
        // Create UI components
        titleLabel = new Label("Processing...");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        messageLabel = new Label("Please wait...");
        messageLabel.setStyle("-fx-font-size: 12px;");
        
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(0);
        
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> handleCancel());
        
        // Layout
        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, messageLabel, progressBar, cancelButton);
        
        Scene scene = new Scene(layout, 350, 150);
        dialogStage.setScene(scene);
        dialogStage.setTitle("S-Emulator");
    }
    
    /**
     * Shows the progress dialog and binds it to a task.
     * 
     * @param task the task to monitor
     * @param title the dialog title
     */
    public void showAndWait(Task<?> task, String title) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.currentTask = task;
        
        // Set title
        if (title != null && !title.trim().isEmpty()) {
            dialogStage.setTitle(title);
            titleLabel.setText(title);
        }
        
        // Bind progress and message
        progressBar.progressProperty().bind(task.progressProperty());
        messageLabel.textProperty().bind(task.messageProperty());
        
        // Handle task completion
        task.setOnSucceeded(e -> {
            dialogStage.close();
        });
        
        task.setOnFailed(e -> {
            dialogStage.close();
        });
        
        task.setOnCancelled(e -> {
            dialogStage.close();
        });
        
        // Cancel button is always enabled for user control
        cancelButton.setDisable(false);
        
        // Handle dialog close request
        dialogStage.setOnCloseRequest(e -> {
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.cancel();
            }
        });
        
        // Start the task in background thread
        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
        
        // Show dialog
        dialogStage.showAndWait();
    }
    
    /**
     * Shows the progress dialog without waiting.
     * 
     * @param task the task to monitor
     * @param title the dialog title
     */
    public void show(Task<?> task, String title) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.currentTask = task;
        
        // Set title
        if (title != null && !title.trim().isEmpty()) {
            dialogStage.setTitle(title);
            titleLabel.setText(title);
        }
        
        // Bind progress and message
        progressBar.progressProperty().bind(task.progressProperty());
        messageLabel.textProperty().bind(task.messageProperty());
        
        // Handle task completion
        task.setOnSucceeded(e -> {
            dialogStage.close();
        });
        
        task.setOnFailed(e -> {
            dialogStage.close();
        });
        
        task.setOnCancelled(e -> {
            dialogStage.close();
        });
        
        // Cancel button is always enabled for user control
        cancelButton.setDisable(false);
        
        // Handle dialog close request
        dialogStage.setOnCloseRequest(e -> {
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.cancel();
            }
        });
        
        // Start the task in background thread
        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
        
        // Show dialog
        dialogStage.show();
    }
    
    /**
     * Closes the progress dialog.
     */
    public void close() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }
        dialogStage.close();
    }
    
    /**
     * Checks if the dialog is currently showing.
     * 
     * @return true if dialog is showing, false otherwise
     */
    public boolean isShowing() {
        return dialogStage.isShowing();
    }
    
    /**
     * Sets whether the cancel button is visible.
     * 
     * @param visible true to show cancel button, false to hide
     */
    public void setCancelButtonVisible(boolean visible) {
        cancelButton.setVisible(visible);
        cancelButton.setManaged(visible);
    }
    
    /**
     * Sets the progress value manually (when not bound to a task).
     * 
     * @param progress the progress value (0.0 to 1.0)
     */
    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }
    
    /**
     * Sets the message text manually (when not bound to a task).
     * 
     * @param message the message to display
     */
    public void setMessage(String message) {
        messageLabel.setText(message);
    }
    
    /**
     * Sets the title text.
     * 
     * @param title the title to display
     */
    public void setTitle(String title) {
        if (title != null && !title.trim().isEmpty()) {
            dialogStage.setTitle(title);
            titleLabel.setText(title);
        }
    }
    
    private void handleCancel() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }
        dialogStage.close();
    }
}
