package fx.util;

import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;

public class FileLoadingProgressDialog {
    
    private final Stage dialogStage;
    private final ProgressBar progressBar;
    private final Label messageLabel;
    private final Label titleLabel;
    private final Label fileNameLabel;
    private final Button cancelButton;
    private Task<?> currentTask;
    
    public FileLoadingProgressDialog(Stage owner) {
        dialogStage = new Stage();
        dialogStage.initStyle(StageStyle.UTILITY);
        dialogStage.initModality(Modality.APPLICATION_MODAL);
        dialogStage.initOwner(owner);
        dialogStage.setResizable(false);
        
        titleLabel = new Label("Loading Program File");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        fileNameLabel = new Label("");
        fileNameLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #7f8c8d; -fx-font-style: italic;");
        
        messageLabel = new Label("Initializing...");
        messageLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #34495e;");
        
        progressBar = new ProgressBar();
        progressBar.setPrefWidth(400);
        progressBar.setProgress(0);
        progressBar.setStyle("-fx-accent: #3498db;");
        
        cancelButton = new Button("Cancel");
        cancelButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-weight: bold;");
        cancelButton.setOnAction(e -> handleCancel());
        
        VBox mainLayout = new VBox(12);
        mainLayout.setAlignment(Pos.CENTER);
        mainLayout.setPadding(new Insets(25));
        
        VBox contentLayout = new VBox(8);
        contentLayout.setAlignment(Pos.CENTER);
        contentLayout.getChildren().addAll(titleLabel, fileNameLabel, messageLabel, progressBar);
        
        HBox buttonLayout = new HBox();
        buttonLayout.setAlignment(Pos.CENTER);
        buttonLayout.getChildren().add(cancelButton);
        
        mainLayout.getChildren().addAll(contentLayout, buttonLayout);
        
        Scene scene = new Scene(mainLayout, 450, 180);
        dialogStage.setScene(scene);
        dialogStage.setTitle("S-Emulator - Loading File");
    }
    
    public void showAndWait(Task<?> task, String fileName) {
        if (task == null) {
            throw new IllegalArgumentException("Task cannot be null");
        }
        
        this.currentTask = task;
        
        if (fileName != null && !fileName.trim().isEmpty()) {
            fileNameLabel.setText("File: " + fileName);
        }
        
        progressBar.progressProperty().bind(task.progressProperty());
        messageLabel.textProperty().bind(task.messageProperty());
        
        task.setOnSucceeded(e -> {
            messageLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            messageLabel.setText("File loaded successfully!");
            progressBar.setProgress(1.0);
            
            Timeline closeTimeline = new Timeline(new KeyFrame(Duration.millis(500), event -> {
                dialogStage.close();
            }));
            closeTimeline.play();
        });
        
        task.setOnFailed(e -> {
            messageLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            messageLabel.setText("Failed to load file");
            messageLabel.setStyle("-fx-text-fill: #e74c3c;");
            
            javafx.application.Platform.runLater(() -> {
                Timeline closeTimeline = new Timeline(new KeyFrame(Duration.millis(2000), event -> {
                    dialogStage.close();
                }));
                closeTimeline.play();
            });
        });
        
        task.setOnCancelled(e -> {
            messageLabel.textProperty().unbind();
            progressBar.progressProperty().unbind();
            messageLabel.setText("File loading cancelled");
            messageLabel.setStyle("-fx-text-fill: #f39c12;");
            
            javafx.application.Platform.runLater(() -> {
                dialogStage.close();
            });
        });
        
        cancelButton.setDisable(false);
        
        dialogStage.setOnCloseRequest(e -> {
            if (currentTask != null && currentTask.isRunning()) {
                currentTask.cancel();
            }
        });

        dialogStage.showAndWait();
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
    
    private void handleCancel() {
        if (currentTask != null && currentTask.isRunning()) {
            currentTask.cancel();
        }
        dialogStage.close();
    }
}
