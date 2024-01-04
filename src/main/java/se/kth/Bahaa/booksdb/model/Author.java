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

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPersonNumber(String personNumber) {
        this.personNumber = personNumber;
    }

    public int getAuthorId() {
        return authorId;
    }
}