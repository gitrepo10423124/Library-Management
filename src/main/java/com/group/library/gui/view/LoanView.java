package com.group.library.gui.view;

import com.group.library.gui.dialogs.LoanDialog;
import com.group.library.model.Loan;
import com.group.library.model.Book;
import com.group.library.model.Member;
import com.group.library.service.LibraryService;
import com.group.library.service.LoanService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;

public class LoanView extends ManagementView<Loan> {

    private final LoanService loanService;
    private final LibraryService libraryService;

    public LoanView(LibraryService libraryService) {
        this.libraryService = libraryService;
        this.loanService = libraryService.loanService;
    }

    @Override
    protected List<Loan> fetchData() {
        return loanService.getAllLoans();
    }

    @Override
    public Scene createScene(Stage stage) {
        stage.setTitle("Loan Management");
        ObservableList<Loan> activeBase = FXCollections.observableArrayList();
        ObservableList<Loan> historyBase = FXCollections.observableArrayList();

        refreshBaseLists(activeBase, historyBase);

        FilteredList<Loan> filteredActive = new FilteredList<>(activeBase, l -> l.getReturnDate() == null);
        FilteredList<Loan> filteredHistory = new FilteredList<>(historyBase, l -> l.getReturnDate() != null);

        TableView<Loan> activeTable = new TableView<>();
        TableView<Loan> historyTable = new TableView<>();

        setupTable(activeTable, false);
        setupTable(historyTable, true);

        SortedList<Loan> sortedActive = new SortedList<>(filteredActive);
        SortedList<Loan> sortedHistory = new SortedList<>(filteredHistory);

        sortedActive.comparatorProperty().bind(activeTable.comparatorProperty());
        sortedHistory.comparatorProperty().bind(historyTable.comparatorProperty());

        activeTable.setItems(sortedActive);
        historyTable.setItems(sortedHistory);

        // Double-click deselect
        activeTable.setRowFactory(tv -> {
            TableRow<Loan> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()
                        && activeTable.getSelectionModel().getSelectedItem() == row.getItem()) {
                    activeTable.getSelectionModel().clearSelection();
                }
            });
            return row;
        });

        historyTable.setRowFactory(tv -> {
            TableRow<Loan> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()
                        && historyTable.getSelectionModel().getSelectedItem() == row.getItem()) {
                    historyTable.getSelectionModel().clearSelection();
                }
            });
            return row;
        });

        // Search
        TextField search = new TextField();
        search.setPromptText("Search by Member or Book");

        search.textProperty().addListener((obs, oldV, newV) -> {
            String f = newV.toLowerCase();

            filteredActive.setPredicate(loan -> {
                Member m = libraryService.memberService.getMemberById(loan.getMemberId());
                Book b = libraryService.bookService.getBookById(loan.getBookId());
                return m.getName().toLowerCase().contains(f)
                        || b.getTitle().toLowerCase().contains(f);
            });

            filteredHistory.setPredicate(loan -> {
                Member m = libraryService.memberService.getMemberById(loan.getMemberId());
                Book b = libraryService.bookService.getBookById(loan.getBookId());
                return (loan.getReturnDate() != null)
                        && (m.getName().toLowerCase().contains(f)
                        || b.getTitle().toLowerCase().contains(f));
            });
        });

        // Buttons
        Button issueBtn = new Button("Issue Loan");
        issueBtn.setOnAction(e -> {
            issueLoan();
            refreshBaseLists(activeBase, historyBase);
        });

        Button returnBtn = new Button("Return Loan");
        returnBtn.setOnAction(e -> {
            returnLoan(activeTable);
            refreshBaseLists(activeBase, historyBase);
        });

        Button deleteBtn = new Button("Delete");
        deleteBtn.setOnAction(e -> {
            deleteLoan(historyTable);
            refreshBaseLists(activeBase, historyBase);
        });

        HBox controls = new HBox(10, search, issueBtn, returnBtn, deleteBtn);
        controls.setPadding(new Insets(10));

        VBox root = new VBox(
                new Label("Active Loans"), activeTable,
                new Label("Loan History"), historyTable,
                controls
        );
        root.setPadding(new Insets(10));
        root.setSpacing(10);

        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        return scene;
    }

    private void setupTable(TableView<Loan> table, boolean includeReturn) {

        TableColumn<Loan, Integer> idCol = new TableColumn<>("Loan ID");
        idCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleIntegerProperty(c.getValue().getId()).asObject());

        TableColumn<Loan, String> memberCol = new TableColumn<>("Member");
        memberCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        libraryService.memberService.getMemberById(c.getValue().getMemberId()).getName()
                ));

        TableColumn<Loan, String> bookCol = new TableColumn<>("Book");
        bookCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        libraryService.bookService.getBookById(c.getValue().getBookId()).getTitle()
                ));

        TableColumn<Loan, String> loanDateCol = new TableColumn<>("Loan Date");
        loanDateCol.setCellValueFactory(c ->
                new javafx.beans.property.SimpleStringProperty(
                        c.getValue().getIssueDate().toString()
                ));

        table.getColumns().addAll(idCol, memberCol, bookCol, loanDateCol);

        if (includeReturn) {
            TableColumn<Loan, String> ret = new TableColumn<>("Return Date");
            ret.setCellValueFactory(c ->
                    new javafx.beans.property.SimpleStringProperty(
                            c.getValue().getReturnDate() != null
                                    ? c.getValue().getReturnDate().toString()
                                    : ""
                    ));
            table.getColumns().add(ret);
        }
    }

    private void refreshBaseLists(ObservableList<Loan> activeBase, ObservableList<Loan> historyBase) {

        List<Loan> all = loanService.getAllLoans();

        activeBase.setAll(
                all.stream()
                        .filter(l -> l.getReturnDate() == null)
                        .toList()
        );

        historyBase.setAll(
                all.stream()
                        .filter(l -> l.getReturnDate() != null)
                        .toList()
        );
    }

    private void issueLoan() {
        Member member = LoanDialog.chooseMember(libraryService.memberService.getAllMembers());
        if (member == null) return;

        Book book = LoanDialog.chooseBook(libraryService.bookService.getAllBooks());
        if (book == null) return;

        boolean ok = loanService.issueBook(book.getId(), member.getId());

        if (!ok) {
            Alert a = new Alert(Alert.AlertType.WARNING, "Cannot issue: book has zero quantity.");
            a.showAndWait();
        }
    }

    private void returnLoan(TableView<Loan> activeTable) {
        Loan selected = activeTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        loanService.returnBook(selected.getId());
    }

    private void deleteLoan(TableView<Loan> historyTable) {
        Loan selected = historyTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;
        loanService.deleteLoan(selected.getId());
    }
}
