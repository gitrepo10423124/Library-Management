package com.group.library.gui;

import com.group.library.gui.view.BookView;
import com.group.library.gui.view.HistoryView;
import com.group.library.gui.view.LoanView;
import com.group.library.gui.view.MemberView;
import com.group.library.service.LibraryService;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MainMenu {

    private final LibraryService libraryService;

    public MainMenu(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    public void start(Stage stage) {
        stage.setTitle("Library Management System");

        Button booksBtn = new Button("Manage Books");
        Button membersBtn = new Button("Manage Members");
        Button loansBtn = new Button("Manage Loans");
        Button historyBtn = new Button("View History");
        historyBtn.setOnAction(e -> new HistoryView(libraryService).show(new Stage()));

        // Open management views in new stages/windows
        booksBtn.setOnAction(e -> new BookView(libraryService).show(new Stage()));
        membersBtn.setOnAction(e -> new MemberView(libraryService).show(new Stage()));
        loansBtn.setOnAction(e -> new LoanView(libraryService).show(new Stage()));

        VBox layout = new VBox(30, booksBtn, membersBtn, loansBtn, historyBtn);
        layout.setPadding(new Insets(20));

        stage.setScene(new Scene(layout, 700, 450));
        stage.getScene().getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.show();
    }
}
