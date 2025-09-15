package fx;

import fx.controller.MainController;
import fx.util.StyleManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class SEmulatorFXApplication extends Application {
    
    private static final String APPLICATION_TITLE = "S-Emulator";
    private static final double MIN_WINDOW_WIDTH = 800.0;
    private static final double MIN_WINDOW_HEIGHT = 600.0;
    private static final double DEFAULT_WINDOW_WIDTH = 1200.0;
    private static final double DEFAULT_WINDOW_HEIGHT = 800.0;

    @Override
    public void start(Stage primaryStage) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fx/main.fxml"));
        Parent root = loader.load();
        

        MainController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
        

        Scene scene = new Scene(root, DEFAULT_WINDOW_WIDTH, DEFAULT_WINDOW_HEIGHT);
        

        StyleManager.applyStylesheet(scene);
        

        primaryStage.setTitle(APPLICATION_TITLE);
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(MIN_WINDOW_WIDTH);
        primaryStage.setMinHeight(MIN_WINDOW_HEIGHT);
        primaryStage.setResizable(true);
        

        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
