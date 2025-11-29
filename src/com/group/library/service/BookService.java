package com.group.library.service;

import com.group.library.model.Book;
import com.group.library.util.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final HistoryService historyService = new HistoryService();

    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();
        String sql = "SELECT * FROM Books";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(new Book(
                        rs.getInt("id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getInt("year"),
                        rs.getBoolean("available"),
                        rs.getInt("quantity")
                ));
            }
            DatabaseManager.commit(conn);
            logger.debug("Retrieved {} books.", books.size());
        } catch (SQLException e) {
            logger.error("Error retrieving all books.", e);
        }
        return books;
    }

    public Book getBookById(int id) {
        String sql = "SELECT * FROM Books WHERE id=?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("year"),
                            rs.getBoolean("available"),
                            rs.getInt("quantity")
                    );
                }
            }
            DatabaseManager.commit(conn);
        } catch (SQLException e) {
            logger.error("Error retrieving book with ID: {}", id, e);
        }
        return null;
    }

    public boolean idExists(int id) {
        return getBookById(id) != null;
    }

    public void addOrUpdateBook(Book book) {
        boolean hasManualId = book.getId() > 0;
        boolean exists = hasManualId && getBookById(book.getId()) != null;

        try (Connection conn = DatabaseManager.getConnection()) {
            if (exists) {
                String sql = "UPDATE Books SET title=?, author=?, year=?, available=?, quantity=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, book.getTitle());
                    ps.setString(2, book.getAuthor());
                    ps.setInt(3, book.getYear());
                    ps.setBoolean(4, book.isAvailable());
                    ps.setInt(5, book.getQuantity());
                    ps.setInt(6, book.getId());
                    ps.executeUpdate();
                    historyService.log("BOOK_UPDATE", "Book updated: " + book.getTitle());
                }
            } else {
                String sql;
                if (hasManualId) {
                    sql = "INSERT INTO Books (id, title, author, year, available, quantity) VALUES (?, ?, ?, ?, ?, ?)";
                } else {
                    sql = "INSERT INTO Books (title, author, year, available, quantity) VALUES (?, ?, ?, ?, ?)";
                }
                try (PreparedStatement ps = hasManualId ?
                        conn.prepareStatement(sql) :
                        conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

                    int idx = 1;
                    if (hasManualId) ps.setInt(idx++, book.getId());
                    ps.setString(idx++, book.getTitle());
                    ps.setString(idx++, book.getAuthor());
                    ps.setInt(idx++, book.getYear());
                    ps.setBoolean(idx++, book.isAvailable());
                    ps.setInt(idx, book.getQuantity());

                    ps.executeUpdate();

                    if (!hasManualId) {
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (keys.next()) book.setId(keys.getInt(1));
                        }
                    }

                    historyService.log(hasManualId ? "BOOK_INSERT" : "BOOK_INSERT", "Book added: " + book.getTitle());
                }
            }
            DatabaseManager.commit(conn);
        } catch (SQLException e) {
            logger.error("Error saving book: {}", book.getTitle(), e);
        }
    }

    public boolean deleteBook(int id) {
        try (Connection conn = DatabaseManager.getConnection()) {
            String check = "SELECT COUNT(*) FROM Loans WHERE book_id=? AND return_date IS NULL";
            try (PreparedStatement psCheck = conn.prepareStatement(check)) {
                psCheck.setInt(1, id);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) > 0) return false;
                }
            }

            try (PreparedStatement psDel = conn.prepareStatement("DELETE FROM Books WHERE id=?")) {
                psDel.setInt(1, id);
                int rows = psDel.executeUpdate();
                if (rows == 0) return false;
            }

            DatabaseManager.commit(conn);
            historyService.log("BOOK_DELETE", "Deleted book ID=" + id);
            return true;
        } catch (SQLException e) {
            logger.error("Error deleting book ID: {}", id, e);
        }
        return false;
    }
}




