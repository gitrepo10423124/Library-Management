package com.group.library.gui.view;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.util.List;

public abstract class ManagementView<T> {
    protected final ObservableList<T> data = FXCollections.observableArrayList();
    protected final FilteredList<T> filteredData = new FilteredList<>(data, p -> true);

    public void show(Stage stage) {
        data.setAll(fetchData());
        Scene scene = createScene(stage);
        stage.setScene(scene);
        stage.show();
    }

    protected abstract List<T> fetchData();
    protected abstract Scene createScene(Stage stage);
}

