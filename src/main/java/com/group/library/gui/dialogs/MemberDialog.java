package com.group.library.gui.dialogs;

import com.group.library.model.Member;
import com.group.library.service.MemberService;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class MemberDialog extends Dialog<Member> {

    public MemberDialog(Member member, MemberService memberService) {
        setTitle(member == null ? "Add Member" : "Edit Member");
        ButtonType saveBtn = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(saveBtn, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(10));

        TextField idField = new TextField(member == null ? "" : String.valueOf(member.getId()));
        TextField nameField = new TextField(member == null ? "" : member.getName());
        TextField emailField = new TextField(member == null ? "" : member.getEmail());
        TextField phoneField = new TextField(member == null ? "" : member.getPhone());

        if (member != null) idField.setDisable(true);

        grid.addRow(0, new Label("ID:"), idField);
        grid.addRow(1, new Label("Name:"), nameField);
        grid.addRow(2, new Label("Email:"), emailField);
        grid.addRow(3, new Label("Phone:"), phoneField);

        getDialogPane().setContent(grid);

        setResultConverter(btn -> {
            if (btn == saveBtn) {
                // 1. Prepare ID value
                int id = 0;
                if (!idField.getText().isBlank()) {
                    try { id = Integer.parseInt(idField.getText().trim()); }
                    catch (NumberFormatException ignored) {}
                }

                // Stop if duplicate ID
                if (member == null && memberService.idExists(id)) {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setHeaderText(null);
                    alert.setContentText("ID " + id + " already exists!");
                    alert.showAndWait();
                    return null;
                }

                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String phone = phoneField.getText().trim();

                return new Member(
                        id,
                        name,
                        email,
                        phone
                );
            }
            return null;
        });
    }
}











