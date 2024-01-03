package se.kth.Bahaa.booksdb.model;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Book {

    private int bookId;
    private String isbn;
    private String title;
    private Date published;
    private List<Author> authors;
    private Genre genre;
    private int rating;

    public Book(int bookId, String isbn, String title, Date published) {
        this.bookId = bookId;
        this.isbn = isbn;
        this.title = title;
        this.published = published;
        this.authors = new ArrayList<>();
    }

    public Book(String isbn, String title, Date published) {
        this(-1, isbn, title, published);
    }

    // Getters and setters
    public int getBookId() { return bookId; }
    public String getIsbn() { return isbn; }
    public String getTitle() { return title; }
    public Date getPublished() { return published; }
    public List<Author> getAuthors() { return authors; }
    public Genre getGenre() { return genre; }
    public int getRating() { return rating; }

    public void setIsbn(String isbn) { this.isbn = isbn; }
    public void setTitle(String title) { this.title = title; }
    public void setPublished(Date published) { this.published = published; }
    public void setAuthors(List<Author> authors) { this.authors = authors; }
    public void setGenre(Genre genre) { this.genre = genre; }
    public void setRating(int rating) { this.rating = rating; }

    public void addAuthor(Author author) {
        if (!authors.contains(author)) {
            authors.add(author);
        }
    }

    public void removeAuthor(Author author) {
        authors.remove(author);
    }

    public String getAuthorNames() {
        return authors.stream().map(Author::getName).collect(Collectors.joining(", "));
    }

    @Override
    public String toString() {
        String authorNames = getAuthorNames();
        return String.format("%s, %s, Published: %s, Authors: %s, Genre: %s, Rating: %d",
                title, isbn, published.toString(), authorNames, genre, rating);
    }
}
