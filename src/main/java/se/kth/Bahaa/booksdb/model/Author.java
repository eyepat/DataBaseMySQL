package se.kth.Bahaa.booksdb.model;

public class Author {
    private int authorId;
    private String name;


    public Author(int authorId, String name) {
        this.authorId = authorId;
        this.name = name;
    }

    public int getAuthorId() { return authorId; }
    public String getName() { return name; }

    // ...
}