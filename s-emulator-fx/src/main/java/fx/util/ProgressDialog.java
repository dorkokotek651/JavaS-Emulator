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

public class ProgressDialog {
    
    private final Stage dialogStage;
    private final ProgressBar progressBar;
    private final Label messageLabel;
    private final Label titleLabel;
    private final Button cancelButton;
    private Task<?> currentTask;
    
    public ProgressDialog(Stage owner) {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setResizable(false);
        

        titleLabel = new Label("Processing...");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        messageLabel = new Label("Please wait...");
        messageLabel.setStyle("-fx-font-size: 12px;");
        
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(300);
        progressBar.setProgress(0);
        
        cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> handleCancel());
        

        VBox layout = new VBox(15);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));
        layout.getChildren().addAll(titleLabel, messageLabel, progressBar, cancelButton);
        
        Scene scene = new Scene(layout, 350, 150);
        dialogStage.setScene(scene);
        dialogStage.setTitle("S-Emulator");
    }
    
    public void showAndWait(Task<?> task, String title) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.currentTask = task;
        

        if (title != null && !title.trim().isEmpty()) {
            dialogStage.setTitle(title);
            titleLabel.setText(title);
        }
        

        progressBar.progressProperty().bind(task.progressProperty());
        messageLabel.textProperty().bind(task.messageProperty());
        

        task.setOnSucceeded(e -> {
            dialogStage.close();
        });
        
        task.setOnFailed(e -> {
            dialogStage.close();
        });
        
        task.setOnCancelled(e -> {
            dialogStage.close();
        });
        

        cancelButton.setDisable(false);
        

        dialogStage.setOnCloseRequest(e -> {
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.cancel();
            }
        });
        

        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
        

        dialogStage.showAndWait();
    }
    
    public void show(Task<?> task, String title) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.currentTask = task;
        

        if (title != null && !title.trim().isEmpty()) {
            dialogStage.setTitle(title);
            titleLabel.setText(title);
        }
        

        progressBar.progressProperty().bind(task.progressProperty());
        messageLabel.textProperty().bind(task.messageProperty());
        

        task.setOnSucceeded(e -> {
            dialogStage.close();
        });
        
        task.setOnFailed(e -> {
            dialogStage.close();
        });
        
        task.setOnCancelled(e -> {
            dialogStage.close();
        });
        

        cancelButton.setDisable(false);
        

        dialogStage.setOnCloseRequest(e -> {
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.cancel();
            }
        });
        

        Thread taskThread = new Thread(task);
        taskThread.setDaemon(true);
        taskThread.start();
        

        dialogStage.show();
    }
    
    public void close() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }
        dialogStage.close();
    }
    
    public boolean isShowing() {
        return dialogStage.isShowing();
    }
    
    public void setCancelButtonVisible(boolean visible) {
        cancelButton.setVisible(visible);
        cancelButton.setManaged(visible);
    }
    
    public void setProgress(double progress) {
        progressBar.setProgress(progress);
    }
    
    public void setMessage(String message) {
        messageLabel.setText(message);
    }
    
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
