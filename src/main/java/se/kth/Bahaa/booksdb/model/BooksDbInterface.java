package se.kth.Bahaa.booksdb.model;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This interface declares methods for querying a Books database.
 * Different implementations of this interface handles the connection and
 * queries to a specific DBMS and database, for example a MySQL or a MongoDB
 * database.
 *
 * NB! The methods in the implementation must catch the SQL/MongoDBExceptions thrown
 * by the underlying driver, wrap in a BooksDbException and then re-throw the latter
 * exception. This way the interface is the same for both implementations, because the
 * exception type in the method signatures is the same. More info in BooksDbException.java.
 * 
 * @author anderslm@kth.se
 */
public interface BooksDbInterface {
    

    boolean connect(String databaseUrl, String username, String password, String databaseName) throws BooksDbException;
    List<Book> getAllBooks() throws BooksDbException;
     void disconnect() throws BooksDbException;

    //me
    void addBook(Book book) throws BooksDbException;

    void updateBook(Book updatedBook) throws BooksDbException;
    void deleteBook(Book book) throws BooksDbException;


        List<Book> searchBooksByTitle(String title) throws BooksDbException;

        List<Book> searchBooksByAuthor(String authorName) throws BooksDbException;
        List<Book> searchBookByISBN(String isbn) throws BooksDbException;
        List<Book> searchBooksByGenre(Genre genre) throws BooksDbException;
        List<Book> searchBooksByRating(int raiting) throws BooksDbException;




    // TODO: Add abstract methods for all inserts, deletes and queries
    // mentioned in the instructions for the assignement.
}
