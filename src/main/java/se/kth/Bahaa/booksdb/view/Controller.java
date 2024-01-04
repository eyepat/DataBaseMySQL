package se.kth.Bahaa.booksdb.view;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import se.kth.Bahaa.booksdb.model.*;

import java.util.List;

import static javafx.scene.control.Alert.AlertType.INFORMATION;
import static javafx.scene.control.Alert.AlertType.WARNING;

public class Controller {
    private final BooksPane booksView;
    private final BooksDbInterface booksDb;

    public Controller(BooksDbInterface booksDb, BooksPane booksView) {
        this.booksDb = booksDb;
        this.booksView = booksView;
    }

    public void connectToDatabase(String url, String user, String pass, String databaseName) {
        Thread thread = new Thread(() -> {
            try {
                boolean isConnected = booksDb.connect(url, user, pass, databaseName);
                if (isConnected) {
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("Connected to database successfully.", INFORMATION);
                        // efreshBooksTable();
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
        thread.start(); // Starta tråden för anslutning
    }

    public static void exitProgram() {
        System.out.println("Exiting the Program!");
        Platform.exit(); // för att avsluta programmet
    }

    public void addNewBook(Book book) {
        Thread thread = new Thread(() -> {
            try {
                booksDb.addBook(book);
                Platform.runLater(this::refreshBooksTable);
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
                Platform.runLater(this::refreshBooksTable);
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
                Platform.runLater(this::refreshBooksTable);
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
                // Kolla efter en icke-tom söksträng, förutom för betyg där en enstaka siffra är giltig
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
                        // ... andra fall ...
                    }
                    if (result == null || result.isEmpty()) {
                        Platform.runLater(() -> {
                            booksView.showAlertAndWait("No results found.", INFORMATION);
                        });
                    } else {
                        List<Book> finalResult = result;
                        Platform.runLater(() -> {
                            booksView.displayBooks(finalResult);
                        });
                    }
                } else {
                    Platform.runLater(() -> {
                        booksView.showAlertAndWait("Enter a search string!", WARNING);
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
        thread.start(); // Starta tråden för att utföra sökningen
    }
}
