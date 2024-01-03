package se.kth.Bahaa.booksdb.model;

public class Author {
    private int authorId;
    private String name;
    private String personNumber;

    public Author(int authorId,String name, String personNumber) {
        this.authorId = authorId;
        this.name = name;
        this.personNumber = personNumber;
    }

    public String getPersonNumber() {
        return personNumber;
    }

    public String getName() {
        return name;
    }

    // ...

    public int getAuthorId() {
        return authorId;
    }
}