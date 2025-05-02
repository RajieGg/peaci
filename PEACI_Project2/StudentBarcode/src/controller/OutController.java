package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import java.io.IOException;

public class OutController {
    
    @FXML private Label timeLabel;
    @FXML private Label dateLabel;
    
    public void setLogData(LocalDateTime time) {
        // Set the time display
        timeLabel.setText(time.format(DateTimeFormatter.ofPattern("hh:mm a")));
        
        // Setup auto-return to dashboard after 3 seconds
        setupAutoReturn();
    }
    
    private void setupAutoReturn() {
        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        
        // Using method reference instead of lambda to avoid event parameter issues
        delay.setOnFinished(this::returnToDashboard);
        
        delay.play();
    }
    
    private void returnToDashboard(javafx.event.Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/display/Dashboard.fxml"));
            Parent root = loader.load();
            
            Stage stage = (Stage) timeLabel.getScene().getWindow();
            stage.setScene(new Scene(root));
            
            // Focus the barcode field again
            DashboardController controller = loader.getController();
            controller.initialize();
        } catch (IOException e) {
            System.err.println("Error returning to dashboard:");
            e.printStackTrace();
            
            // Fallback: try again on JavaFX thread
            javafx.application.Platform.runLater(() -> {
                try {
                    returnToDashboard(event);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }
    }
}