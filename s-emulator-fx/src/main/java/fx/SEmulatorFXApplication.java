package fx;

import fx.controller.MainController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main JavaFX application class for S-Emulator.
 * This class serves as the entry point for the graphical user interface.
 */
public class SEmulatorFXApplication extends Application {
    
    private static final String APPLICATION_TITLE = "S-Emulator";
    private static final double MIN_WINDOW_WIDTH = 800.0;
    private static final double MIN_WINDOW_HEIGHT = 600.0;
    private static final double DEFAULT_WINDOW_WIDTH = 1200.0;
    private static final double DEFAULT_WINDOW_HEIGHT = 800.0;

    @Override
    public void start(Stage primaryStage) throws IOException {
        // Load main FXML layout
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/main.fxml"));
        Parent root = loader.load();
        
        // Get controller and set primary stage reference
        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        
        // Create scene
        Scene scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        
        // Configure primary stage
        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        
        // Show the application
        primaryStage.show();
    }

    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}
