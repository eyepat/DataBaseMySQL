package se.kth.Bahaa.booksdb.model;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BooksDbMockImpl implements BooksDbInterface {
    private Connection connection;
    private final List<Book> books;

    public BooksDbMockImpl() {
        this.books = new ArrayList<>();
    }


    @Override
    public boolean connect(String databaseUrl, String username, String password, String databaseName) throws BooksDbException {
        try {
            // Construct the full database URL including the database name
            String fullDatabaseUrl = databaseUrl + databaseName;

            connection = DriverManager.getConnection(fullDatabaseUrl, username, password);
            populateBooksFromDatabase();
            return true;
        } catch (SQLException e) {
            throw new BooksDbException("Failed to connect to the database: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Book> getAllBooks() throws BooksDbException {
        List<Book> allBooks = new ArrayList<>();
        String query = "SELECT * FROM Book";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int bookId = resultSet.getInt("BookId");
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year");
                Genre genre = Genre.valueOf(resultSet.getString("Genre"));
                int rating = resultSet.getInt("Rating");

                Book book = new Book(bookId, isbn, title, publishedDate); // Create book with BookId
                book.setGenre(genre);
                book.setRating(rating);
                book.setAuthors(getAuthorsForBook(bookId)); // Fetch authors for each book using BookId

                allBooks.add(book);
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error retrieving all books from the database: " + e.getMessage());
        }
        return allBooks;
    }

    @Override
    public void disconnect() throws BooksDbException {
        try {
            if (connection != null) {
                if (!connection.isClosed()) {
                    connection.close();
                    System.out.println("Disconnected from the database.");
                } else {
                    System.out.println("Connection is already closed.");
                }
            }
        } catch (SQLException e) {
            throw new BooksDbException("Failed to close the database connection: " + e.getMessage(), e);
        }
    }


    private List<Author> getAuthorsForBook(int bookId) throws SQLException {
        List<Author> authors = new ArrayList<>();
        String query = "SELECT a.AuthorId, a.Name FROM Author a JOIN BookAuthor ba ON a.AuthorId = ba.AuthorId WHERE ba.BookId = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, bookId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int authorId = resultSet.getInt("AuthorId");
                String name = resultSet.getString("Name");
                authors.add(new Author(authorId, name, null)); // Assuming there's no personNumber needed here
            }
        }
        return authors;
    }


    private void populateBooksFromDatabase() {
        try {
            // Create a SQL query to retrieve all books
            String query = "SELECT * FROM Book";

            // Create a PreparedStatement and execute the query
            PreparedStatement statement = connection.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();

            // Iterate through the ResultSet and create Book objects
            while (resultSet.next()) {
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year"); // Assuming the column name is "published"
                // Add more fields as needed

                // Create a Book object and add it to the data model
                Book book = new Book(isbn, title, publishedDate); // Create the Book object with appropriate fields
                books.add(book); // Add the book to the data model
            }

            // Close the ResultSet and PreparedStatement
            resultSet.close();
            statement.close();

        } catch (SQLException e) {
            // Handle exceptions
        }
    }

    // ... Rest of the class ...



    @Override
    public void addBook(Book book) throws BooksDbException {
        // SQL statements
        String insertBookSql = "INSERT INTO Book (isbn, title, publication_year, Genre, Rating) VALUES (?, ?, ?, ?, ?)";
        String insertAuthorSql = "INSERT INTO Author (Name) VALUES (?)"; // Assuming Name is enough for a new author
        String insertBookAuthorSql = "INSERT INTO BookAuthor (BookId, AuthorId) VALUES (?, ?)";

        try {
            connection.setAutoCommit(false);

            // Insert book details and get generated BookId
            int bookId;
            try (PreparedStatement bookStmt = connection.prepareStatement(insertBookSql, Statement.RETURN_GENERATED_KEYS)) {
                bookStmt.setString(1, book.getIsbn());
                bookStmt.setString(2, book.getTitle());
                bookStmt.setDate(3, book.getPublished());
                bookStmt.setString(4, book.getGenre().toString());
                bookStmt.setInt(5, book.getRating());
                bookStmt.executeUpdate();

                ResultSet rs = bookStmt.getGeneratedKeys();
                if (rs.next()) {
                    bookId = rs.getInt(1);
                } else {
                    throw new BooksDbException("Failed to insert book, no ID obtained.");
                }
            }

            // Insert authors and get their Ids, then link them to the book
            for (Author author : book.getAuthors()) {
                int authorId;

                // Check if author exists, if not, insert and get the generated AuthorId
                // This requires additional logic to check for existing authors which is not shown here

                // Assuming a new author is always added for simplicity
                try (PreparedStatement authorStmt = connection.prepareStatement(insertAuthorSql, Statement.RETURN_GENERATED_KEYS)) {
                    authorStmt.setString(1, author.getName());
                    authorStmt.executeUpdate();

                    ResultSet rs = authorStmt.getGeneratedKeys();
                    if (rs.next()) {
                        authorId = rs.getInt(1);
                    } else {
                        throw new BooksDbException("Failed to insert author, no ID obtained.");
                    }
                }

                // Link book and author
                try (PreparedStatement linkStmt = connection.prepareStatement(insertBookAuthorSql)) {
                    linkStmt.setInt(1, bookId);
                    linkStmt.setInt(2, authorId);
                    linkStmt.executeUpdate();
                }
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // Log or handle rollback exception
                }
            }
            throw new BooksDbException("Failed to add book and authors: " + e.getMessage(), e);
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                // Log or handle exception
            }
        }
    }



    public void updateBook(Book updatedBook) throws BooksDbException {
        // Update book details first
        String updateBookSql = "UPDATE Book SET isbn = ?, title = ?, publication_year = ?, Genre = ?, Rating = ? WHERE BookId = ?";
        try (PreparedStatement bookStmt = connection.prepareStatement(updateBookSql)) {
            bookStmt.setString(1, updatedBook.getIsbn());
            bookStmt.setString(2, updatedBook.getTitle());
            bookStmt.setDate(3, updatedBook.getPublished());
            bookStmt.setString(4, updatedBook.getGenre().toString());
            bookStmt.setInt(5, updatedBook.getRating());
            bookStmt.setInt(6, updatedBook.getBookId());
            bookStmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("Error updating book details: " + e.getMessage(), e);
        }

        // Update authors and book-author associations
        updateAuthorsAndAssociations(updatedBook);
    }
    private void updateAuthorsAndAssociations(Book updatedBook) throws BooksDbException {
        // First, update authors' details
        for (Author author : updatedBook.getAuthors()) {
            updateAuthor(author);
        }

        // Then, update book-author associations
        updateBookAuthors(updatedBook.getBookId(), updatedBook.getAuthors());
    }
    private void updateBookAuthors(int bookId, List<Author> authors) throws BooksDbException {
        // Delete existing book-author associations
        String deleteAssociationsSql = "DELETE FROM BookAuthor WHERE BookId = ?";
        try (PreparedStatement deleteStmt = connection.prepareStatement(deleteAssociationsSql)) {
            deleteStmt.setInt(1, bookId);
            deleteStmt.executeUpdate();
        } catch (SQLException e) {
            throw new BooksDbException("Error deleting book-author associations: " + e.getMessage(), e);
        }

        // Add new book-author associations
        String insertAssociationSql = "INSERT INTO BookAuthor (BookId, AuthorId) VALUES (?, ?)";
        for (Author author : authors) {
            try (PreparedStatement insertStmt = connection.prepareStatement(insertAssociationSql)) {
                insertStmt.setInt(1, bookId);
                insertStmt.setInt(2, author.getAuthorId());
                insertStmt.executeUpdate();
            } catch (SQLException e) {
                throw new BooksDbException("Error adding book-author association: " + e.getMessage(), e);
            }
        }
    }

    public void updateAuthor(Author author) throws BooksDbException {
        String updateAuthorSql = "UPDATE Author SET Name = ?, PersonNumber = ? WHERE AuthorId = ?";
        try (PreparedStatement stmt = connection.prepareStatement(updateAuthorSql)) {
            stmt.setString(1, author.getName());
            stmt.setString(2, author.getPersonNumber());
            stmt.setInt(3, author.getAuthorId());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new BooksDbException("No author was updated. Check the AuthorId: " + author.getAuthorId());
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error updating author: " + e.getMessage(), e);
        }
    }




    @Override
    public void deleteBook(Book book) throws BooksDbException {
        // SQL statements for deleting the book and its associations
        String deleteBookAuthorsSql = "DELETE FROM BookAuthor WHERE BookId = ?";
        String deleteBookSql = "DELETE FROM Book WHERE BookId = ?";

        try {
            connection.setAutoCommit(false);

            // Delete associations from BookAuthor table
            try (PreparedStatement deleteAuthorsStmt = connection.prepareStatement(deleteBookAuthorsSql)) {
                deleteAuthorsStmt.setInt(1, book.getBookId());
                deleteAuthorsStmt.executeUpdate();
            }

            // Delete the book from Book table
            try (PreparedStatement deleteBookStmt = connection.prepareStatement(deleteBookSql)) {
                deleteBookStmt.setInt(1, book.getBookId());
                deleteBookStmt.executeUpdate();
            }

            connection.commit();
        } catch (SQLException e) {
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException ex) {
                    // Handle rollback exception
                }
            }
            throw new BooksDbException("Error removing book: " + e.getMessage(), e);
        }

        // Remove the book from the in-memory list
        books.remove(book);
    }







    @Override
    public List<Book> searchBooksByTitle(String searchTitle) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        searchTitle = searchTitle.toLowerCase();
        String query = "SELECT * FROM Book WHERE LOWER(title) LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + searchTitle + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int bookId = resultSet.getInt("BookId");
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year");
                Genre genre = Genre.valueOf(resultSet.getString("Genre"));
                int rating = resultSet.getInt("Rating");

                Book book = new Book(bookId, isbn, title, publishedDate); // Create book with BookId
                book.setGenre(genre);
                book.setRating(rating);
                book.setAuthors(getAuthorsForBook(bookId)); // Fetch authors for each book using BookId
                result.add(book);
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by title: " + e.getMessage());
        }

        return result;
    }



    @Override
    public List<Book> searchBooksByAuthor(String authorName) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        String query = "SELECT b.BookId, b.isbn, b.title, b.publication_year, b.Genre, b.Rating FROM Book b " +
                "JOIN BookAuthor ba ON b.BookId = ba.BookId " +
                "JOIN Author a ON ba.AuthorId = a.AuthorId " +
                "WHERE LOWER(a.Name) LIKE ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, "%" + authorName.toLowerCase() + "%");
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int bookId = resultSet.getInt("BookId");
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year");
                Genre genre = Genre.valueOf(resultSet.getString("Genre"));
                int rating = resultSet.getInt("Rating");

                Book book = new Book(bookId, isbn, title, publishedDate);
                book.setGenre(genre);
                book.setRating(rating);
                book.setAuthors(getAuthorsForBook(bookId)); // Adjust this method to work with bookId
                result.add(book);
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by author: " + e.getMessage());
        }

        return result;
    }



    @Override
    public List<Book> searchBookByISBN(String isbn) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        String query = "SELECT * FROM Book WHERE isbn = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, isbn);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int bookId = resultSet.getInt("BookId"); // Retrieve BookId
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year");
                Genre genre = Genre.valueOf(resultSet.getString("Genre"));
                int rating = resultSet.getInt("Rating");

                Book book = new Book(bookId, isbn, title, publishedDate);
                book.setGenre(genre);
                book.setRating(rating);
                book.setAuthors(getAuthorsForBook(bookId)); // Use BookId to fetch authors
                result.add(book);
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error searching book by ISBN: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Book> searchBooksByGenre(Genre genre) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        String query = "SELECT * FROM Book WHERE Genre = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, genre.toString());
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int bookId = resultSet.getInt("BookId");
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year");
                int rating = resultSet.getInt("Rating");

                Book book = new Book(bookId, isbn, title, publishedDate);
                book.setGenre(genre);
                book.setRating(rating);
                book.setAuthors(getAuthorsForBook(bookId)); // Use BookId to fetch authors
                result.add(book);
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by genre: " + e.getMessage());
        }



        return result;
    }

    @Override
    public List<Book> searchBooksByRating(int rating) throws BooksDbException {
        List<Book> result = new ArrayList<>();
        String query = "SELECT * FROM Book WHERE Rating = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, rating);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int bookId = resultSet.getInt("BookId");
                String isbn = resultSet.getString("isbn");
                String title = resultSet.getString("title");
                Date publishedDate = resultSet.getDate("publication_year");
                Genre genre = Genre.valueOf(resultSet.getString("Genre"));

                Book book = new Book(bookId, isbn, title, publishedDate);
                book.setGenre(genre);
                book.setRating(rating);
                book.setAuthors(getAuthorsForBook(bookId)); // Use BookId to fetch authors
                result.add(book);
            }
        } catch (SQLException e) {
            throw new BooksDbException("Error searching books by rating: " + e.getMessage());
        }

        return result;
    }






    private static final Book[] DATA = {
            new Book(1, "123456789", "Databases Illuminated", new Date(2018, 1, 1)),
            new Book(2, "234567891", "Dark Databases", new Date(1990, 1, 1)),
            new Book(3, "456789012", "The buried giant", new Date(2000, 1, 1)),
            new Book(4, "567890123", "Never let me go", new Date(2000, 1, 1)),
            new Book(5, "678901234", "The remains of the day", new Date(2000, 1, 1)),
            new Book(6, "234567890", "Alias Grace", new Date(2000, 1, 1)),
            new Book(7, "345678911", "The handmaids tale", new Date(2010, 1, 1)),
            new Book(8, "345678901", "Shuggie Bain", new Date(2020, 1, 1)),
            new Book(9, "345678912", "Microserfs", new Date(2000, 1, 1)),


    };
}
