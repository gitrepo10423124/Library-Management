package com.group.library.gui.dialogs;

import com.group.library.model.Book;
import com.group.library.model.Member;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.List;

public class LoanDialog {
    public static Member chooseMember(List<Member> members) {
        Dialog<Member> dialog = new Dialog<>();
        dialog.setTitle("Select Member");
        ButtonType selectBtn = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);

        ComboBox<Member> combo = new ComboBox<>();
        combo.getItems().addAll(members);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Member item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.getName()); }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));

        VBox box = new VBox(combo); box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> btn == selectBtn ? combo.getValue() : null);
        return dialog.showAndWait().orElse(null);
    }

    public static Book chooseBook(List<Book> books) {
        List<Book> availableBooks = books.stream().filter(Book::isAvailable).toList();
        Dialog<Book> dialog = new Dialog<>();
        dialog.setTitle("Select Book");
        ButtonType selectBtn = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(selectBtn, ButtonType.CANCEL);

        ComboBox<Book> combo = new ComboBox<>();
        combo.getItems().addAll(availableBooks);
        combo.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Book item, boolean empty) { super.updateItem(item, empty); setText(empty || item == null ? "" : item.getTitle()); }
        });
        combo.setButtonCell(combo.getCellFactory().call(null));

        VBox box = new VBox(combo); box.setPadding(new Insets(10));
        dialog.getDialogPane().setContent(box);
        dialog.setResultConverter(btn -> btn == selectBtn ? combo.getValue() : null);
        return dialog.showAndWait().orElse(null);
    }
}


