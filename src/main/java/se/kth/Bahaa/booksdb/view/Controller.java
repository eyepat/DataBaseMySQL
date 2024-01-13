package se.kth.Bahaa.booksdb.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import se.kth.Bahaa.booksdb.model.*;

import java.util.List;
import java.util.Properties;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class Controller {
    private final BooksPane booksView;
    private final BooksDbInterface booksDb;
    private Properties dbProperties;

    public Controller(BooksDbInterface booksDb, BooksPane booksView, Properties dbProperties) {
        this.booksDb = booksDb;
        this.booksView = booksView;
        this.dbProperties = dbProperties;
    }

    public void connectToDatabase() {
        Thread thread = new Thread(() -> {
            try {
                // Extract database connection details from the properties
                String url = dbProperties.getProperty("db.url");
                String user = dbProperties.getProperty("db.user");
                String password = dbProperties.getProperty("db.password");
                String databaseName = dbProperties.getProperty("db.name"); // Add this line in your properties file

                boolean isConnected = booksDb.connect(url, user, password, databaseName);
                if (isConnected) {
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("Connected to database successfully.", Alert.AlertType.INFORMATION);
                        // Refresh books table if needed
                    });
                } else {
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("Failed to connect to the database.", Alert.AlertType.ERROR);
                    });
                }
            } catch (BooksDbException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
        thread.start(); // Start the thread for connection
    }


    public static void exitProgram() {
        System.out.println("Exiting the Program!");
        Platform.exit(); // för att avsluta programmet
    }

    public void addNewBook(Book book) {
        Thread thread = new Thread(() -> {
            try {
                booksDb.addBook(book);
                //Platform.runLater(this::refreshBooksTable);
            } catch (BooksDbException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Error adding book: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
        thread.start(); // Starta tråden för att lägga till boken
    }

    public void removeBook(Book book) {
        Thread thread = new Thread(() -> {
            try {
                booksDb.deleteBook(book);
                //Platform.runLater(this::refreshBooksTable);
            } catch (BooksDbException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Error removing book: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
        thread.start(); // Starta tråden för att ta bort boken
    }

    public void updateBook(Book updatedBook) {
        Thread thread = new Thread(() -> {
            try {
                booksDb.updateBook(updatedBook);
                //Platform.runLater(this::refreshBooksTable);
            } catch (BooksDbException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Error updating book: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
        thread.start(); // Starta tråden för att uppdatera boken
    }

    private void refreshBooksTable() {
        Thread thread = new Thread(() -> {
            try {
                List<Book> books = booksDb.getAllBooks();
                Platform.runLater(() -> {
                    booksView.displayBooks(books);
                });
            } catch (BooksDbException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Error fetching books: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
        thread.start(); // Starta tråden för att uppdatera boktabellen
    }

    public void onSearchSelected(String searchFor, SearchMode mode) {
        Thread thread = new Thread(() -> {
            try {
                // Check if the search string is null or empty
                if ((mode != SearchMode.RATING && (searchFor == null || searchFor.trim().isEmpty()))) {
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("Enter a search string!", Alert.AlertType.WARNING);
                    });
                    return; // Exit the method if the search string is empty
                }

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
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("No results found.", Alert.AlertType.INFORMATION);
                    });
                } else {
                    List<Book> finalResult = result;
                    Platform.runLater(() -> {
                        booksView.displayBooks(finalResult);
                    });
                }
            } catch (NumberFormatException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Invalid format.", Alert.AlertType.ERROR);
                });
            } catch (BooksDbException e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Database error: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    booksView.showAlertAndWait("Unexpected error: " + e.getMessage(), Alert.AlertType.ERROR);
                });
            }
        });
        thread.start(); // Start the thread to perform the search
    }
}
