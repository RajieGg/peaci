import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import controller.AutoOutService;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Start automatic OUT service
        AutoOutService autoOutService = new AutoOutService();
        autoOutService.start();

        // Correct the path to your FXML file
        Parent root = FXMLLoader.load(getClass().getResource("/display/Dashboard.fxml"));
        primaryStage.setTitle("PEACI Barcode System");
        primaryStage.setScene(new Scene(root));
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}