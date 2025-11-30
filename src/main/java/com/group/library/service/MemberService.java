package com.group.library.service;

import com.group.library.model.Member;
import com.group.library.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final HistoryService historyService = new HistoryService();

    public List<Member> getAllMembers() {
        List<Member> members = new ArrayList<>();
        String sql = "SELECT * FROM Members";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                members.add(new Member(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone")
                ));
            }
            DatabaseManager.commit(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving all members.", e);
        }
        return members;
    }

    public Member getMemberById(int id) {
        String sql = "SELECT * FROM Members WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Member(
                            rs.getInt("id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("phone")
                    );
                }
            }
            DatabaseManager.commit(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving member with ID: {}", id, e);
        }
        return null;
    }

    public boolean idExists(int id) {
        return getMemberById(id) != null;
    }

    public void addOrUpdateMember(Member member) {
        boolean hasManualId = member.getId() > 0;
        boolean exists = hasManualId && getMemberById(member.getId()) != null;

        if (exists) {
            try (Connection conn = DatabaseManager.getConnection()) {
                String sql = "UPDATE Members SET name=?, email=?, phone=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, member.getName());
                    ps.setString(2, member.getEmail());
                    ps.setString(3, member.getPhone());
                    ps.setInt(4, member.getId());
                    ps.executeUpdate();
                    DatabaseManager.commit(conn);
                    historyService.log("MEMBER_UPDATE", "Updated member: " + member.getName());
                }
            } catch (SQLException e) {
                logger.error("Error updating member: {}", member.getName(), e);
            }
            return;
        }

        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = hasManualId
                    ? "INSERT INTO Members (id, name, email, phone) VALUES (?, ?, ?, ?)"
                    : "INSERT INTO Members (name, email, phone) VALUES (?, ?, ?)";
            try (PreparedStatement ps = hasManualId
                    ? conn.prepareStatement(sql)
                    : conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                int idx = 1;
                if (hasManualId) ps.setInt(idx++, member.getId());
                ps.setString(idx++, member.getName());
                ps.setString(idx++, member.getEmail());
                ps.setString(idx, member.getPhone());
                ps.executeUpdate();

                if (!hasManualId) {
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (keys.next()) member.setId(keys.getInt(1));
                    }
                }

                DatabaseManager.commit(conn);
                historyService.log(hasManualId ? "MEMBER_INSERT" : "MEMBER_INSERT", "Added member: " + member.getName());
            }
        } catch (SQLException e) {
            logger.error("Error adding member: {}", member.getName(), e);
        }
    }

    public boolean deleteMember(int id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String check = "SELECT COUNT(*) FROM Loans WHERE member_id=? AND return_date IS NULL";
            try (PreparedStatement psCheck = conn.prepareStatement(check)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return false;
                }
            }

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM Members WHERE id=?")) {
                psDel.setInt(1, id);
                int rows = psDel.executeUpdate();
                if (rows == 0) return false;
            }

            DatabaseManager.commit(conn);
            historyService.log("MEMBER_DELETE", "Deleted member ID=" + id);
            return true;
        } catch (SQLException e) {
            logger.error("Error deleting member ID: {}", id, e);
        }
        return false;
    }
}



