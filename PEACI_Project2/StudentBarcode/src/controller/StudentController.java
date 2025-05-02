package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.Map;

public class StudentController {
    
    @FXML private ImageView studentImageView;
    @FXML private Label nameLabel;
    @FXML private Label courseLabel;
    @FXML private Label idLabel;
    @FXML private Label expiryLabel;

    public void setStudentData(Map<String, String> studentData) {
        try {
            // Set basic student info
            nameLabel.setText(studentData.get("name"));
            courseLabel.setText(studentData.get("course"));
            idLabel.setText(studentData.get("id"));
            
            // Set expiry info if available
            if (expiryLabel != null) {
                expiryLabel.setText("Expired: " + studentData.get("expiryDate"));
                startAutoReturn(); // Simulan ang auto-return kapag may expiry info
            }
            
            // Load image
            // In StudentController.java:
            String imagePath = "/images/" + studentData.get("photo_path");
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            studentImageView.setImage(image);
            
        } catch (Exception e) {
            System.err.println("Error loading student data: " + e.getMessage());
        }
    }

    private void startAutoReturn() {
        PauseTransition delay = new PauseTransition(Duration.seconds(5));
        delay.setOnFinished(event -> returnToDashboard());
        delay.play();
    }

    private void returnToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/display/Dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) expiryLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            // Focus ulit sa barcode field
            DashboardController controller = loader.getController();
            controller.initialize();
            
        } catch (Exception e) {
            System.err.println("Error returning to dashboard: " + e.getMessage());
        }
    }
}