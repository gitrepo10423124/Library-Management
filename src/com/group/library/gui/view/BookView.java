package com.group.library.gui.view;

import com.group.library.gui.dialogs.BookDialog;
import com.group.library.model.Book;
import com.group.library.service.LibraryService;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class BookView extends ManagementView<Book> {

    private final LibraryService libraryService;

    public BookView(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @Override
    protected List<Book> fetchData() {
        return libraryService.bookService.getAllBooks();
    }

    @Override
    protected Scene createScene(Stage stage) {
        stage.setTitle("Book Management");
        TableView<Book> table = new TableView<>();
        TableColumn<Book, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Book, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        TableColumn<Book, String> authorCol = new TableColumn<>("Author");
        authorCol.setCellValueFactory(new PropertyValueFactory<>("author"));
        TableColumn<Book, Integer> quantityCol = new TableColumn<>("Quantity");
        quantityCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));

        table.getColumns().addAll(idCol, titleCol, authorCol, quantityCol);

        table.setRowFactory(tv -> {
            TableRow<Book> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    if (table.getSelectionModel().getSelectedItem() == row.getItem()) {
                        table.getSelectionModel().clearSelection();
                    }
                }
            });
            return row;
        });

        SortedList<Book> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        TextField searchField = new TextField();
        searchField.setPromptText("Search Title or Author");
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String filter = newV.toLowerCase();
            filteredData.setPredicate(book ->
                    book.getTitle().toLowerCase().contains(filter) ||
                            book.getAuthor().toLowerCase().contains(filter));
        });

        Button addBtn = new Button("Add/Edit Book");
        addBtn.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            BookDialog dialog = new BookDialog(selected, libraryService.bookService);
            dialog.showAndWait().ifPresent(b -> {
                libraryService.bookService.addOrUpdateBook(b);
                data.setAll(fetchData());
            });
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Book selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean deleted = libraryService.bookService.deleteBook(selected.getId());
                if (!deleted) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot delete: book has active loans.");
                    alert.showAndWait();
                }
                data.setAll(fetchData());
            }
        });

        HBox controls = new HBox(10, searchField, addBtn, deleteBtn);
        controls.setPadding(new Insets(10));

        BorderPane layout = new BorderPane();
        layout.setCenter(table);
        layout.setBottom(controls);

        Scene scene = new Scene(layout, 700, 450);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }
}




