package com.group.library.gui.view;

import com.group.library.gui.dialogs.MemberDialog;
import com.group.library.model.Member;
import com.group.library.service.LibraryService;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory; // New import needed
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.List;

public class MemberView extends ManagementView<Member> {

    private final LibraryService libraryService;

    public MemberView(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @Override
    protected List<Member> fetchData() {
        return libraryService.memberService.getAllMembers();
    }

    @Override
    protected Scene createScene(Stage stage) {
        stage.setTitle("Member Management");
        TableView<Member> table = new TableView<>();

        TableColumn<Member, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id")); // Cleaner access

        TableColumn<Member, String> nameCol = new TableColumn<>("Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); // Cleaner access

        TableColumn<Member, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email")); // Cleaner access

        TableColumn<Member, String> phoneCol = new TableColumn<>("Phone");
        phoneCol.setCellValueFactory(new PropertyValueFactory<>("phone")); // Cleaner access

        // This complex column MUST keep the manual binding
        TableColumn<Member, Integer> loanCol = new TableColumn<>("Active Loan");
        loanCol.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty(
                        (int) libraryService.loanService.countOngoingLoansForMember(data.getValue().getId())
                ).asObject());


        table.getColumns().addAll(idCol, nameCol, emailCol, phoneCol, loanCol);

        table.setRowFactory(tv -> {
            TableRow<Member> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    if (table.getSelectionModel().getSelectedItem() == row.getItem()) {
                        table.getSelectionModel().clearSelection();
                    }
                }
            });
            return row;
        });

        SortedList<Member> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(table.comparatorProperty());
        table.setItems(sortedData);

        TextField searchField = new TextField();
        searchField.setPromptText("Search by Name/Email/Phone");
        searchField.textProperty().addListener((obs, oldV, newV) -> {
            String filter = newV.toLowerCase();
            filteredData.setPredicate(m ->
                    m.getName().toLowerCase().contains(filter) ||
                            m.getEmail().toLowerCase().contains(filter) ||
                            m.getPhone().toLowerCase().contains(filter));
        });

        Button addBtn = new Button("Add/Edit Member");
        addBtn.setOnAction(e -> {
            Member selected = table.getSelectionModel().getSelectedItem();
            MemberDialog dialog = new MemberDialog(selected, libraryService.memberService);
            dialog.showAndWait().ifPresent(m -> {
                libraryService.memberService.addOrUpdateMember(m);
                data.setAll(fetchData());
            });
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            Member selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                boolean deleted = libraryService.memberService.deleteMember(selected.getId());
                if (!deleted) {
                    Alert alert = new Alert(Alert.AlertType.WARNING, "Cannot delete: member has active loans.");
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




