package com.group.library.gui.dialogs;

import com.group.library.model.Book;
import com.group.library.service.BookService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.Node;

public class BookDialog extends Dialog<Book> {

    public BookDialog(Book book, BookService bookService) {
        setTitle(book == null ? "Add Book" : "Edit Book");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(book == null ? "" : String.valueOf(book.getId()));
        TextField titleField = new TextField(book == null ? "" : book.getTitle());
        TextField authorField = new TextField(book == null ? "" : book.getAuthor());
        TextField quantityField = new TextField(book == null ? "1" : String.valueOf(book.getQuantity()));

        if (book != null) idField.setDisable(true);

        grid.addRow(0, new Label("ID:"), idField);
        grid.addRow(1, new Label("Title:"), titleField);
        grid.addRow(2, new Label("Author:"), authorField);
        grid.addRow(3, new Label("Quantity:"), quantityField);

        getDialogPane().setContent(grid);

        Node saveNode = getDialogPane().lookupButton(saveBtn);
        saveNode.setDisable(true);

        Runnable validate = () -> {
            boolean valid = !titleField.getText().isBlank() && !authorField.getText().isBlank();
            try {
                int q = Integer.parseInt(quantityField.getText().trim());
                if (q < 0) valid = false;
            } catch (Exception ex) {
                valid = false;
            }
            saveNode.setDisable(!valid);
        };
        titleField.textProperty().addListener((o,a,b) -> validate.run());
        authorField.textProperty().addListener((o,a,b) -> validate.run());
        quantityField.textProperty().addListener((o,a,b) -> validate.run());
        validate.run();

        setResultConverter(btn -> {
            if (btn == saveBtn) {
                int id = 0;
                if (!idField.getText().isBlank()) {
                    try { id = Integer.parseInt(idField.getText().trim()); } catch (NumberFormatException ignored) {}
                }

                if (book == null && bookService.idExists(id)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setContentText("ID " + id + " already exists!");
                    alert.showAndWait();
                    return null;
                }

                int qty = 1;
                try { qty = Integer.parseInt(quantityField.getText().trim()); } catch (NumberFormatException ignored) {}

                String title = titleField.getText().trim();
                String author = authorField.getText().trim();

                return new Book(
                        id,
                        title,
                        author,
                        0,
                        qty > 0,
                        qty
                );
            }
            return null;
        });
    }
}




