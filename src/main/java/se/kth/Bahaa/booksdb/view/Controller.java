package se.kth.Bahaa.booksdb.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import se.kth.Bahaa.booksdb.model.*;

import java.util.List;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class Controller {
    private  final BooksPane booksView;
    private final BooksDbInterface booksDb;

    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    public void connectToDatabase(String url, String user, String pass, String databaseName) {
        try {
            boolean isConnected = booksDb.connect(url, user, pass, databaseName);
            if (isConnected) {
                booksView.showAlertAndWait("Connected to database successfully.", INFORMATION);
                refreshBooksTable();
            } else {
                booksView.showAlertAndWait("Failed to connect to the database.", Alert.AlertType.ERROR);
            }
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }


    public static void exitProgram() {
        System.out.println("Exiting the Program!");
        Platform.exit(); // for exiting the game.
    }
    public void addNewBook(Book book) {
        try {
            booksDb.addBook(book);
            refreshBooksTable();
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Error adding book: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }



    public void removeBook(Book book) {
        try {
            booksDb.deleteBook(book);
            refreshBooksTable();
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Error removing book: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public void updateBook(Book updatedBook) {
        try {
            booksDb.updateBook(updatedBook);
            refreshBooksTable();
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Error updating book: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void refreshBooksTable() {
        try {
            List<Book> books = booksDb.getAllBooks();
            booksView.displayBooks(books);
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Error fetching books: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }
    public void onSearchSelected(String searchFor, SearchMode mode) {
        try {
            // Check for a non-empty search string, except for rating where a single digit is valid
            if (searchFor != null && (mode != SearchMode.RATING || !searchFor.trim().isEmpty())) {
                List<Book> result = null;
                switch (mode) {
                    case Title:

                        result = booksDb.searchBooksByTitle(searchFor);
                        break;
                    case ISBN:
                        result = booksDb.searchBookByISBN(searchFor);
                        break;
                    case Author:
                        result = booksDb.searchBooksByAuthor(searchFor);
                        break;
                    case GENRE:
                        Genre genre = Genre.valueOf(searchFor.toUpperCase());
                        result = booksDb.searchBooksByGenre(genre);
                        break;
                    case RATING:
                        int rating = Integer.parseInt(searchFor);
                            result = booksDb.searchBooksByRating(rating);
                        break;
                    // ... other cases ...
                }
                if (result == null || result.isEmpty()) {
                    booksView.showAlertAndWait("No results found.", INFORMATION);
                } else {
                    booksView.displayBooks(result);
                }
            } else {
                booksView.showAlertAndWait("Enter a search string!", WARNING);
            }
        }  catch (NumberFormatException e) {
            booksView.showAlertAndWait("Invalid format.", Alert.AlertType.ERROR);
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            booksView.showAlertAndWait("Unexpected error: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }





        // TODO:
    // Add methods for all types of user interaction (e.g. via  menus).
}
