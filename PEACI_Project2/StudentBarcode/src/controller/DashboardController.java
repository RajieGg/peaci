package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DashboardController {

    @FXML
    private TextField hiddenBarcodeField;

    @FXML
    public void initialize() {
        hiddenBarcodeField.requestFocus();
    }

    @FXML
    public void handleKeyInput(KeyEvent event) {
        if (event.getCode() == KeyCode.ENTER) {
            String barcode = hiddenBarcodeField.getText().trim();
            if (!barcode.isEmpty()) {
                System.out.println("\n=== Processing barcode: " + barcode + " ===");
                processBarcode(barcode);
                hiddenBarcodeField.clear();
            }
        }
    }

    private void processBarcode(String barcode) {
        Map<String, String> studentData = fetchStudentData(barcode);

        if (studentData == null || studentData.get("newid") == null) {
            System.err.println("⚠️ Invalid or missing student data!");
            showErrorScreen("Invalid Student ID");
            return;
        }

        System.out.println("Student Data: " + studentData);

        if (isBarcodeExpired(studentData.get("expiryDate"))) {
            showExpiredScreen(studentData);
            return;
        }

        if (isEligibleForOut(barcode)) {
            showOutScreen(studentData);
        } else {
            showInScreen(studentData);
        }
    }

    private boolean isBarcodeExpired(String expiryDate) {
        try {
            if (expiryDate == null || expiryDate.isEmpty()) {
                return true;
            }

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate expiry = LocalDate.parse(expiryDate, formatter);
            LocalDate today = LocalDate.now();

            System.out.println("Current Date: " + today);
            System.out.println("Expiry Date: " + expiry);

            return today.isAfter(expiry);
        } catch (Exception e) {
            System.err.println("❌ Error parsing expiry date: " + e.getMessage());
            return true;
        }
    }

    private void showExpiredScreen(Map<String, String> studentData) {
        try {
            System.out.println("Loading Error.fxml...");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/display/Error.fxml"));
            Parent root = loader.load();

            StudentController controller = loader.getController();
            controller.setStudentData(studentData);

            Stage stage = (Stage) hiddenBarcodeField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            System.err.println("❌ Error loading expired screen: " + e.getMessage());
        }
    }

    private void logAttendance(String id, String status) throws SQLException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Invalid student ID");
        }

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/peaci_db",
                "root",
                "")) {

            String query = "INSERT INTO attendance_logs (newid, status, time_log) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, id);
            stmt.setString(2, status);
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));

            int rowsAffected = stmt.executeUpdate();
            System.out.println("✅ Logged " + status + " for ID: " + id + " (" + rowsAffected + " rows affected)");
        }
    }

    private void showOutScreen(Map<String, String> studentData) {
        try {
            logAttendance(studentData.get("newid"), "OUT");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/display/Out.fxml"));
            Parent root = loader.load();

            OutController controller = loader.getController();
            controller.setLogData(LocalDateTime.now());

            Stage stage = (Stage) hiddenBarcodeField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            System.err.println("❌ Error in OUT screen: " + e.getMessage());
        }
    }

    private void showInScreen(Map<String, String> studentData) {
        try {
            logAttendance(studentData.get("newid"), "IN");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/display/Pass.fxml"));
            Parent root = loader.load();

            PassController controller = loader.getController();
            controller.setStudentData(studentData);

            Stage stage = (Stage) hiddenBarcodeField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            System.err.println("❌ Error in IN screen: " + e.getMessage());
        }
    }

    private boolean isEligibleForOut(String barcode) {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/peaci_db",
                "root",
                "")) {

            String query = "SELECT time_log FROM attendance_logs "
                    + "WHERE newid = ? AND status = 'IN' "
                    + "ORDER BY time_log DESC LIMIT 1";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, barcode);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                LocalDateTime lastIn = rs.getTimestamp("time_log").toLocalDateTime();
                LocalDateTime now = LocalDateTime.now();

                System.out.println("Last IN: " + lastIn);
                System.out.println("Current Time: " + now);
                System.out.println(
                        "Time Difference: " + java.time.Duration.between(lastIn, now).toMinutes() + " minutes");

                return now.isAfter(lastIn.plusMinutes(5));
            }
        } catch (SQLException e) {
            System.err.println("❌ Database error in eligibility check: " + e.getMessage());
        }
        return false;
    }

    private Map<String, String> fetchStudentData(String barcode) {
        Map<String, String> studentData = new HashMap<>();

        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/peaci_db",
                "root",
                "")) {

            String query = "SELECT name, course, newid, photo_path, expiry_date "
                    + "FROM student WHERE newid = ?";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, barcode);

            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                studentData.put("newid", rs.getString("newid"));
                studentData.put("name", rs.getString("name"));
                studentData.put("course", rs.getString("course"));
                studentData.put("photoPath", rs.getString("photo_path"));
                studentData.put("expiryDate", rs.getString("expiry_date"));

                System.out.println("✅ Found student: " + studentData.get("name"));
            } else {
                System.out.println("⚠️ No student found for ID: " + barcode);
                return null;
            }

        } catch (SQLException e) {
            System.err.println("❌ Database error: " + e.getMessage());
        }

        return studentData;
    }

    private void showErrorScreen(String message) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/display/Error.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) hiddenBarcodeField.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            System.err.println("❌ Critical error loading error screen: " + e.getMessage());
        }
    }
}