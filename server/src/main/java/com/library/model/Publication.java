package com.library.model;

public class Publication {
    private int id;
    private String title;
    private String author;
    private int yearPublished;
    private String genre;
    private int totalCopies;
    private int availableCopies;

    public Publication(int id, String title, String author, int yearPublished, String genre, int totalCopies, int availableCopies) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public Publication(String title, String author, int yearPublished, String genre, int totalCopies, int availableCopies) {
        this.title = title;
        this.author = author;
        this.yearPublished = yearPublished;
        this.genre = genre;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public int getYearPublished() { return yearPublished; }
    public String getGenre() { return genre; }
    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
}