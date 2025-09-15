package fx.util;

import engine.exception.SProgramException;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

public class ErrorDialogUtil {
    
    public static void showError(String title, String message) {
        showError(null, title, message);
    }
    
    public static void showError(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title != null ? title : "Error");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "An unknown error occurred");
        
        alert.showAndWait();
    }
    
    public static void showDetailedError(String title, String message, String details) {
        showDetailedError(null, title, message, details);
    }
    
    public static void showDetailedError(Stage owner, String title, String message, String details) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.initOwner(owner);
        alert.setTitle(title != null ? title : "Error");
        alert.setHeaderText(message != null ? message : "An error occurred");
        
        if (details != null && !details.trim().isEmpty()) {

            TextArea textArea = new TextArea(details);
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setMaxWidth(Double.MAX_VALUE);
            textArea.setMaxHeight(Double.MAX_VALUE);
            
            alert.getDialogPane().setExpandableContent(textArea);
        }
        
        alert.showAndWait();
    }
    
    public static void showWarning(String title, String message) {
        showWarning(null, title, message);
    }
    
    public static void showWarning(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.initOwner(owner);
        alert.setTitle(title != null ? title : "Warning");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "A warning occurred");
        
        alert.showAndWait();
    }
    
    public static void showInformation(String title, String message) {
        showInformation(null, title, message);
    }
    
    public static void showInformation(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle(title != null ? title : "Information");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "Information");
        
        alert.showAndWait();
    }
    
    public static boolean showConfirmation(String title, String message) {
        return showConfirmation(null, title, message);
    }
    
    public static boolean showConfirmation(Stage owner, String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.initOwner(owner);
        alert.setTitle(title != null ? title : "Confirmation");
        alert.setHeaderText(null);
        alert.setContentText(message != null ? message : "Are you sure?");
        
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }
    
    public static String getFormattedErrorMessage(Exception exception) {
        if (exception == null) {
            return "Unknown error occurred";
        }
        
        String message = exception.getMessage();
        if (message == null || message.trim().isEmpty()) {
            message = exception.getClass().getSimpleName();
        }
        

        if (exception instanceof SProgramException) {
            return "Program Error: " + message;
        } else if (exception instanceof IllegalArgumentException) {
            return "Invalid Input: " + message;
        } else if (message.contains("FileNotFoundException")) {
            return "File Not Found: The selected file could not be found";
        } else if (message.contains("AccessDeniedException")) {
            return "Access Denied: You don't have permission to read this file";
        } else {
            return message;
        }
    }
}
