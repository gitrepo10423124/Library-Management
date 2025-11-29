package com.group.library.gui;

import com.group.library.service.LibraryService;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        LibraryService libraryService = new LibraryService();
        new MainMenu(libraryService).start(primaryStage);
    }

    public static void main(String[] args) {
        launch();
    }
}








