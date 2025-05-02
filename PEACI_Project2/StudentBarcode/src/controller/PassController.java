package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.Map;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class PassController {

    @FXML
    private Label nameLabel;
    @FXML
    private Label courseLabel;
    @FXML
    private Label idLabel;
    @FXML
    private Label timeLabel;
    @FXML
    private ImageView studentImageView;

    public void setStudentData(Map<String, String> studentData) {
        try {
            // Set student name
            nameLabel.setText(studentData.get("name"));
            courseLabel.setText(studentData.get("course"));
            idLabel.setText(studentData.get("newid"));

            // Set current time
            timeLabel.setText(LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));

            // Load image from resources
            String imagePath = studentData.get("photoPath");
            if (imagePath != null && !imagePath.isEmpty()) {
                // Gamitin ang resource path imbes na absolute path
                Image image = new Image(getClass().getResourceAsStream("/" + imagePath));
                studentImageView.setImage(image);
            } else {
                System.err.println("⚠️ Walang larawan ang estudyanteng ito");
            }

        } catch (Exception e) {
            System.err.println("❌ Error sa pag-load ng student data:");
            e.printStackTrace();
        }
    }
}