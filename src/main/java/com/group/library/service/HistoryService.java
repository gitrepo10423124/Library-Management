package com.group.library.service;

import com.group.library.model.History;
import com.group.library.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryService {
    private static final Logger logger = LoggerFactory.getLogger(HistoryService.class);

    public void log(String actionType, String description) {
        String sql = "INSERT INTO History (action_type, description) VALUES (?, ?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, actionType);
            ps.setString(2, description);
            ps.executeUpdate();
            DatabaseManager.commit(conn);

            logger.info("History recorded: [Action Type: {}] {}", actionType, description);

        } catch (SQLException e) {
            logger.error("Failed to log history item [{}]: {}", actionType, description, e);
        }
    }

    public List<History> getAllHistory() {
        List<History> list = new ArrayList<>();
        String sql = "SELECT * FROM History ORDER BY action_time DESC";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new History(
                        rs.getInt("id"),
                        rs.getString("action_type"),
                        rs.getString("description"),
                        rs.getTimestamp("action_time").toLocalDateTime()
                ));
            }
            DatabaseManager.commit(conn);

        } catch (SQLException e) {
            logger.error("Error retrieving all history records.", e);
        }
        return list;
    }

    public void clearHistory() {
        String sql = "DELETE FROM History";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            int rows = ps.executeUpdate();
            DatabaseManager.commit(conn);
            logger.warn("History cleared. Deleted {} records.", rows);

        } catch (SQLException e) {
            logger.error("Error clearing history records.", e);
        }
    }
}

