package com.group.library.model;

public class Book {
    private int id;
    private String title;
    private String author;
    private int year;           // still kept for compatibility
    private boolean available;
    private int quantity;

    public Book(int id, String title, String author, int year, boolean available, int quantity) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.year = year;
        this.available = available;
        this.quantity = quantity;
    }

    // Compatibility constructor
    public Book(int id, String title, String author, int year, boolean available) {
        this(id, title, author, year, available, available ? 1 : 0);
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getYear() { return year; }
    public void setYear(int year) { this.year = year; }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity = quantity;
        this.available = quantity > 0;
    }
}


