package com.PCLP2.Project.data.entity;

import java.util.Random;

public class Book {
    private int id, releaseYear;
    private String bookName, authorName, genre, format;
    private String ISBN, ISSN, locationCode;

    public Book() {}

    public Book(String bookName, String authorName, String genre, String format, int releaseYear, String ISBN, String ISSN) {
        super();
        this.bookName = bookName;
        this.authorName = authorName;
        this.genre = genre;
        this.format = format;
        this.releaseYear = releaseYear;
        this.ISBN = ISBN;
        this.ISSN = ISSN;
        this.locationCode = generateLocationCode();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getBookName() {
        return bookName;
    }

    public void setBookName(String bookName) {
        this.bookName = bookName;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getFormat() { return format; }

    public void setFormat(String format) { this.format = format; }

    public int getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getISBN() {
        return ISBN;
    }

    public void setISBN(String ISBNcode) {
        if(ISBNcode.length() != 19)
            return;
        this.ISBN = ISBNcode;
    }

    public String getISSN() {
        return ISSN;
    }

    public void setISSN(String ISSNcode) {
        if(ISSNcode.length() != 9)
            return;
        this.ISSN = ISSNcode;
    }

    public String getLocationCode() {
        return locationCode;
    }

    public void setLocationCode(String locationCode) {
        if(locationCode.length() != 5)
            return;
        this.locationCode = locationCode;
    }

    public void regenerateLocCode() {
        locationCode = generateLocationCode();
    }

    @Override
    public String toString() {
        return bookName + " de " + authorName + ", gen: " + genre + ", format: " + format + ", anul lansarii: " + releaseYear;
    }

    private String generateLocationCode() {
        StringBuilder code = new StringBuilder();

        Random rand = new Random();
        int nr;

        do {
            nr = rand.nextInt(10);
            code.append(nr);
        } while(code.length() < 5);

        return code.toString();
    }
}
