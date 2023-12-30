package se.kth.Bahaa.booksdb.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import se.kth.Bahaa.booksdb.model.*;

import java.util.ArrayList;
import java.util.List;

import static javafx.scene.control.Alert.AlertType.*;

/**
 * The controller is responsible for handling user requests and update the view
 * (and in some cases the model).
 *
 * @author anderslm@kth.se
 */
public class Controller {

    private static BooksPane booksView = null; // view
    private static BooksDbInterface booksDb = null; // model

    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    public static void exitProgram() {
        System.out.println("Exiting the Program!");
        Platform.exit(); // for exiting the game.
    }
    public static void addNewBook(Book book) {
        try {
            booksDb.addBook(book);
            // Update view or show confirmation
        } catch (BooksDbException e) {
            booksView.showAlertAndWait("Error adding book to the database.",ERROR);
        }
    }
    protected void onSearchSelected(String searchFor, SearchMode mode) {
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
