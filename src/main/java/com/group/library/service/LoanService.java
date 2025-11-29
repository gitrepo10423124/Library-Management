package com.group.library.service;

import com.group.library.model.Loan;
import com.group.library.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LoanService {
    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);
    private final HistoryService historyService = new HistoryService();

    public List<Loan> getAllLoans() {
        List<Loan> loans = new ArrayList<>();
        String sql = "SELECT * FROM Loans";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                loans.add(new Loan(
                        rs.getInt("id"),
                        rs.getInt("book_id"),
                        rs.getInt("member_id"),
                        rs.getDate("issue_date").toLocalDate(),
                        rs.getDate("return_date") != null ? rs.getDate("return_date").toLocalDate() : null
                ));
            }
            DatabaseManager.commit(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving all loans.", e);
        }
        return loans;
    }

    public boolean issueBook(int bookId, int memberId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement psCheck = conn.prepareStatement("SELECT quantity FROM Books WHERE id=?")) {
                psCheck.setInt(1, bookId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next()) {
                        if (rs.getInt("quantity") <= 0) return false;
                    } else return false;
                }
            }

            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO Loans (book_id, member_id, issue_date) VALUES (?, ?, ?)", Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, bookId);
                ps.setInt(2, memberId);
                ps.setDate(3, Date.valueOf(LocalDate.now()));
                ps.executeUpdate();
            }

            try (PreparedStatement psUpd = conn.prepareStatement(
                    "UPDATE Books SET quantity = quantity - 1, available = CASE WHEN quantity - 1 > 0 THEN 1 ELSE 0 END WHERE id=?")) {
                psUpd.setInt(1, bookId);
                psUpd.executeUpdate();
            }

            DatabaseManager.commit(conn);
            historyService.log("LOAN_ISSUE", "Issued book ID=" + bookId + " to member ID=" + memberId);
            return true;
        } catch (SQLException e) {
            logger.error("Error issuing book ID {} to member ID {}.", bookId, memberId, e);
        }
        return false;
    }

    public void returnBook(int loanId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String sql = "UPDATE Loans SET return_date=? WHERE id=? AND return_date IS NULL";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, Date.valueOf(LocalDate.now()));
                ps.setInt(2, loanId);
                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (PreparedStatement psBook = conn.prepareStatement(
                            "UPDATE Books SET quantity = quantity + 1, available = 1 WHERE id=(SELECT book_id FROM Loans WHERE id=?)")) {
                        psBook.setInt(1, loanId);
                        psBook.executeUpdate();
                    }
                }
            }
            DatabaseManager.commit(conn);
            historyService.log("LOAN_RETURN", "Returned loan ID=" + loanId);
        } catch (SQLException e) {
            logger.error("Error returning loan ID: {}", loanId, e);
        }
    }

    public void deleteLoan(int loanId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM Loans WHERE id=?")) {
            ps.setInt(1, loanId);
            int rows = ps.executeUpdate();
            DatabaseManager.commit(conn);
            if (rows > 0) historyService.log("LOAN_DELETE", "Deleted loan ID=" + loanId);
        } catch (SQLException e) {
            logger.error("Error deleting loan ID: {}", loanId, e);
        }
    }

    public long countOngoingLoansForMember(int memberId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT COUNT(*) FROM Loans WHERE member_id=? AND return_date IS NULL")) {
            ps.setInt(1, memberId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong(1);
            }
            DatabaseManager.commit(conn);
        } catch (SQLException e) {
            logger.error("Error counting ongoing loans for member ID: {}", memberId, e);
        }
        return 0;
    }
}

