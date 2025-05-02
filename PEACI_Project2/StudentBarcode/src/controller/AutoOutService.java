package controller;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Timer;
import java.util.TimerTask;

public class AutoOutService {
    private static final LocalTime START_TIME = LocalTime.of(6, 0); // 6AM
    private static final LocalTime END_TIME = LocalTime.of(22, 0); // 10PM

    public void start() {
        Timer timer = new Timer();
        // Schedule task to run every day at 10:01 PM
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (isDuringOperatingHours()) {
                    processAutoOut();
                }
            }
        }, getInitialDelay(), 24 * 60 * 60 * 1000); // Run daily
    }

    private long getInitialDelay() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextRun = now.withHour(22).withMinute(1).withSecond(0);
        if (now.isAfter(nextRun)) {
            nextRun = nextRun.plusDays(1);
        }
        return java.time.Duration.between(now, nextRun).toMillis();
    }

    private boolean isDuringOperatingHours() {
        LocalTime now = LocalTime.now();
        return !now.isBefore(START_TIME) && !now.isAfter(END_TIME);
    }

    private void processAutoOut() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/peaci_db",
                "root",
                "")) {

            // Find students who checked IN today but didn't check OUT
            String query = "SELECT DISTINCT newid FROM attendance_logs " +
                    "WHERE DATE(time_log) = CURDATE() " +
                    "AND status = 'IN' " +
                    "AND newid NOT IN " +
                    "(SELECT newid FROM attendance_logs " +
                    "WHERE DATE(time_log) = CURDATE() " +
                    "AND status = 'OUT')";

            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    String studentId = rs.getString("newid");
                    logAutoOut(studentId);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void logAutoOut(String studentId) throws SQLException {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/peaci_db",
                "root",
                "")) {

            String query = "INSERT INTO attendance_logs (newid, status, time_log, auto_out) " +
                    "VALUES (?, ?, ?, ?)";

            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, studentId);
            stmt.setString(2, "OUT");
            stmt.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            stmt.setBoolean(4, true); // Mark as automatic OUT
            stmt.executeUpdate();
        }
    }
}