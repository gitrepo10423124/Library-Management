package com.group.library.gui.view;

import com.group.library.model.History;
import com.group.library.service.LibraryService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class HistoryView extends ManagementView<History> {

    private final LibraryService libraryService;
    private TableView<History> table;

    public HistoryView(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @Override
    protected List<History> fetchData() {
        return libraryService.historyService.getAllHistory();
    }

    @Override
    protected Scene createScene(Stage stage) {
        stage.setTitle("History Management");
        table = new TableView<>();
        table.setItems(data);

        // Columns
        TableColumn<History, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));

        TableColumn<History, String> typeCol = new TableColumn<>("Action");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("actionType"));

        TableColumn<History, String> descCol = new TableColumn<>("Description");
        descCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        descCol.setPrefWidth(300);

        TableColumn<History, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(new PropertyValueFactory<>("actionTime"));

        table.getColumns().addAll(idCol, typeCol, descCol, timeCol);

        // Clear history button
        Button clearBtn = new Button("Clear History");
        clearBtn.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm");
            confirm.setHeaderText("Clear All History?");
            confirm.setContentText("This will permanently delete every log entry.");
            confirm.showAndWait().ifPresent(result -> {
                if (result == ButtonType.OK) {
                    libraryService.historyService.clearHistory();
                    data.setAll(fetchData());
                }
            });
        });

        HBox buttonBar = new HBox(10, clearBtn);
        buttonBar.setPadding(new Insets(10));

        // Layout
        BorderPane layout = new BorderPane();
        layout.setCenter(table);
        layout.setBottom(buttonBar);

        Scene scene = new Scene(layout, 900, 450);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

        return scene;
    }
}




