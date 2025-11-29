package com.group.library.model;

import java.time.LocalDateTime;

public class History {
    private int id;
    private String actionType;
    private String description;
    private LocalDateTime actionTime;

    public History(int id, String actionType, String description, LocalDateTime actionTime) {
        this.id = id;
        this.actionType = actionType;
        this.description = description;
        this.actionTime = actionTime;
    }

    public int getId() { return id; }
    public String getActionType() { return actionType; }
    public String getDescription() { return description; }
    public LocalDateTime getActionTime() { return actionTime; }
}

