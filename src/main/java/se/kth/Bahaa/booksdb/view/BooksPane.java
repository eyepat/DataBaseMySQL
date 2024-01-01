    package se.kth.Bahaa.booksdb.view;

    import java.sql.Date;
    import java.time.LocalDate;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.List;
    import java.util.Optional;
    import java.util.stream.Collectors;

    import javafx.collections.FXCollections;
    import javafx.collections.ObservableList;
    import javafx.event.ActionEvent;
    import javafx.event.EventHandler;
    import javafx.geometry.Insets;
    import javafx.geometry.Pos;
    import javafx.scene.control.*;
    import javafx.scene.control.cell.PropertyValueFactory;
    import javafx.scene.layout.*;
    import javafx.stage.Popup;
    import se.kth.Bahaa.booksdb.model.*;


    /**
     * The main pane for the view, extending VBox and including the menus. An
     * internal BorderPane holds the TableView for books and a search utility.
     *
     * @author anderslm@kth.se
     */
    public class BooksPane extends VBox {

        private TableView<Book> booksTable;
        private ObservableList<Book> booksInTable; // the data backing the table view

        private ComboBox<SearchMode> searchModeBox;
        private TextField searchField;
        private Button searchButton;
        private ObservableList<Author> authorList = FXCollections.observableArrayList();
        private MenuBar menuBar;

        public BooksPane(BooksDbMockImpl booksDb) {
            final Controller controller = new Controller(booksDb, this);
            this.init(controller);
        }

        /**
         * Display a new set of books, e.g. from a database select, in the
         * booksTable table view.
         *
         * @param books the books to display
         */
        public void displayBooks(List<Book> books) {
            booksInTable.clear();
            booksInTable.addAll(books);
        }

        /**
         * Notify user on input error or exceptions.
         *
         * @param msg  the message
         * @param type types: INFORMATION, WARNING et c.
         */
        protected void showAlertAndWait(String msg, Alert.AlertType type) {
            // types: INFORMATION, WARNING et c.
            Alert alert = new Alert(type, msg);
            alert.showAndWait();
        }

        private void init(Controller controller) {

            booksInTable = FXCollections.observableArrayList();

            // init views and event handlers
            initBooksTable();
            initSearchView(controller);
            initMenus();

            FlowPane bottomPane = new FlowPane();
            bottomPane.setHgap(10);
            bottomPane.setPadding(new Insets(10, 10, 10, 10));
            bottomPane.getChildren().addAll(searchModeBox, searchField, searchButton);

            BorderPane mainPane = new BorderPane();
            mainPane.setCenter(booksTable);
            mainPane.setBottom(bottomPane);
            mainPane.setPadding(new Insets(10, 10, 10, 10));

            this.getChildren().addAll(menuBar, mainPane);
            VBox.setVgrow(mainPane, Priority.ALWAYS);
        }

        private void initBooksTable() {
            booksTable = new TableView<>();
            booksTable.setEditable(false); // don't allow user updates (yet)
            booksTable.setPlaceholder(new Label("No rows to display"));

            // define columns
            TableColumn<Book, String> titleCol = new TableColumn<>("Title");
            TableColumn<Book, String> isbnCol = new TableColumn<>("ISBN");
            TableColumn<Book, Date> publishedCol = new TableColumn<>("Published");
            TableColumn<Book, String> authorCol = new TableColumn<>("Authors");
            TableColumn<Book, String> genreCol = new TableColumn<>("Genre");
            TableColumn<Book, Integer> ratingCol = new TableColumn<>("Rating");
            booksTable.getColumns().addAll(titleCol, isbnCol, publishedCol, authorCol, genreCol, ratingCol);

            // give title column some extra space
            titleCol.prefWidthProperty().bind(booksTable.widthProperty().multiply(0.5));

            // define how to fill data for each cell,
            // get values from Book properties
            titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
            isbnCol.setCellValueFactory(new PropertyValueFactory<>("isbn"));
            publishedCol.setCellValueFactory(new PropertyValueFactory<>("published"));
            authorCol.setCellValueFactory(new PropertyValueFactory<>("authorNames"));
            genreCol.setCellValueFactory(new PropertyValueFactory<>("genre"));
            ratingCol.setCellValueFactory(new PropertyValueFactory<>("rating"));

            // associate the table view with the data
            booksTable.setItems(booksInTable);
        }

        private void initSearchView(Controller controller) {
            searchField = new TextField();
            searchField.setPromptText("Search for...");

            ComboBox<Genre> genreSearchComboBox = new ComboBox<>();
            genreSearchComboBox.getItems().addAll(Genre.values());

            Popup genrePopup = new Popup();
            genrePopup.getContent().add(genreSearchComboBox);

            searchModeBox = new ComboBox<>();
            searchModeBox.getItems().addAll(SearchMode.values());
            searchModeBox.setValue(SearchMode.Title);

            searchButton = new Button("Search");

            //search mode changes
            searchModeBox.setOnAction(e -> {
                if (searchModeBox.getValue() == SearchMode.GENRE) {
                    searchField.setVisible(false);

                    genrePopup.show(searchModeBox,
                            searchModeBox.getScene().getWindow().getX() + searchModeBox.localToScene(searchModeBox.getBoundsInLocal()).getMinX() + 110,
                            searchModeBox.getScene().getWindow().getY() + searchModeBox.localToScene(searchModeBox.getBoundsInLocal()).getMaxY() + 4);
                } else {
                    searchField.setVisible(true);
                    genrePopup.hide();
                }
            });

            // search button action
            searchButton.setOnAction(e -> {
                if (searchModeBox.getValue() == SearchMode.GENRE) {
                    Genre selectedGenre = genreSearchComboBox.getValue();
                    controller.onSearchSelected(selectedGenre.toString(), searchModeBox.getValue());
                }else {
                        String searchText = searchField.getText();
                        controller.onSearchSelected(searchText, searchModeBox.getValue());
                    }

            });

            HBox searchBox = new HBox(10);
            searchBox.setAlignment(Pos.CENTER_LEFT);
            searchBox.getChildren().addAll(searchModeBox, searchField, searchButton);

            this.getChildren().add(0, searchBox); // Add at the top of the VBox
        }
        private void RemoveSelected() {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                showAlertAndWait("No Book Selected", Alert.AlertType.WARNING);
                return;
            }

            Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmationAlert.setTitle("Confirm Delete");
            confirmationAlert.setHeaderText("Delete Book");
            confirmationAlert.setContentText("Are you sure you want to delete the selected book?");
            Optional<ButtonType> result = confirmationAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Controller.removeBook(selectedBook);
                booksInTable.remove(selectedBook); // Remove the book from the observable list
            }
        }

        private void updateSelectedBook() {
            Book selectedBook = booksTable.getSelectionModel().getSelectedItem();
            if (selectedBook == null) {
                showAlertAndWait("No Book Selected", Alert.AlertType.WARNING);
                return;
            }

            // Create and show the update dialog
            Dialog<Book> updateDialog = ShowUpdateBookDialog(selectedBook);
            Optional<Book> result = updateDialog.showAndWait();

            result.ifPresent(updatedBook -> {
                // Call the controller to update the book
                Controller.updateBook(updatedBook);
                // Refresh the table view
                displayBooks(new ArrayList<>(booksInTable)); // Assuming booksInTable contains all books
            });
        }

        private void updateAuthorsToString(Book book, String authorsStr) {
            List<Author> newAuthors = Arrays.stream(authorsStr.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(Author::new)
                    .collect(Collectors.toList());
            book.setAuthors(newAuthors);
        }
        private void initMenus() {
            Menu fileMenu = new Menu("File");
            MenuItem exitItem = new MenuItem("Exit");
            exitItem.setOnAction(e -> Controller.exitProgram());

            MenuItem connectItem = new MenuItem("Connect to Db");
            MenuItem disconnectItem = new MenuItem("Disconnect");
            fileMenu.getItems().addAll(exitItem, connectItem, disconnectItem);

            Menu manageMenu = new Menu("Manage");
            MenuItem addItem = new MenuItem("Add");
            addItem.setOnAction(e -> showAddBookDialog());
            MenuItem removeItem = new MenuItem("Remove");
            removeItem.setOnAction(e -> RemoveSelected());
            MenuItem updateItem = new MenuItem("Update");
            updateItem.setOnAction(e -> updateSelectedBook());
            manageMenu.getItems().addAll(addItem, removeItem, updateItem);
            menuBar = new MenuBar();
            menuBar.getMenus().addAll(fileMenu, manageMenu);
        }



    //Manage the add book stuff :)
        private void showAddBookDialog() {
            authorList.clear();
            // Create the custom dialog.
            Dialog<Book> dialog = new Dialog<>();
            dialog.setTitle("Add New Book");

            // Set the button types.
            ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

            // Create the book form.
            GridPane gridPane = new GridPane();
            gridPane.setHgap(10);
            gridPane.setVgap(10);
            gridPane.setPadding(new Insets(20, 150, 10, 10));

            TextField titleField = new TextField();
            titleField.setPromptText("Title");
            TextField isbnField = new TextField();
            isbnField.setPromptText("ISBN");
            DatePicker publishedPicker = new DatePicker();

            ComboBox<Genre> genreComboBox = new ComboBox<>();
            genreComboBox.getItems().addAll(Genre.values());
            genreComboBox.setValue(Genre.FANTASY); // Default value

            ComboBox<Integer> ratingComboBox = new ComboBox<>();
            ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
            ratingComboBox.setValue(1); // Default rating

            ListView<Author> authorsListView = new ListView<>(authorList);
            TextField authorNameField = new TextField();
            authorNameField.setPromptText("Author's Name");
            Button addAuthorButton = new Button("Add Author");
            addAuthorButton.setOnAction(e -> {
                Author newAuthor = new Author(authorNameField.getText()); // Assuming a constructor that takes a name
                authorList.add(newAuthor);
                authorNameField.clear();
            });

            // Add components to the grid
            gridPane.add(new Label("Title:"), 0, 0);
            gridPane.add(titleField, 1, 0);
            gridPane.add(new Label("ISBN:"), 0, 1);
            gridPane.add(isbnField, 1, 1);
            gridPane.add(new Label("Published Date:"), 0, 2);
            gridPane.add(publishedPicker, 1, 2);
            gridPane.add(new Label("Genre:"), 0, 3);
            gridPane.add(genreComboBox, 1, 3); // Replaced genreField with genreComboBox
            gridPane.add(authorNameField, 1, 5);
            gridPane.add(addAuthorButton, 2, 5);
            gridPane.add(new Label("Rating:"), 0, 6);
            gridPane.add(ratingComboBox, 1, 6);

            dialog.getDialogPane().setContent(gridPane);

            // Convert the result to a Book when the add button is clicked.
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == addButtonType) {
                    String title = titleField.getText();
                    String isbn = isbnField.getText();
                    LocalDate publishedDate = publishedPicker.getValue();
                    Date published = Date.valueOf(publishedDate);

                    Book newBook = new Book(isbn, title, published);
                    authorList.forEach(newBook::addAuthor);

                    Genre selectedGenre = genreComboBox.getValue();
                    newBook.setGenre(selectedGenre);

                    int rating = ratingComboBox.getValue();
                    newBook.setRating(rating);

                    return newBook;
                }
                return null;
            });

            // Show the dialog and capture the result.
            Optional<Book> result = dialog.showAndWait();
            result.ifPresent(book -> {
                // Handle the result (add the book to the database)
                Controller.addNewBook(book);
            });
        }

        //Manage the update book stuff :)
        private Dialog<Book>ShowUpdateBookDialog(Book book) {
            Dialog<Book> dialog = new Dialog<>();
            dialog.setTitle("Update Book");

            // Set the button types
            ButtonType updateButtonType = new ButtonType("Update", ButtonBar.ButtonData.OK_DONE);
            dialog.getDialogPane().getButtonTypes().addAll(updateButtonType, ButtonType.CANCEL);

            // Create the update form
            GridPane grid = new GridPane();
            grid.setHgap(10);
            grid.setVgap(10);
            grid.setPadding(new Insets(20, 150, 10, 10));

            TextField isbnField = new TextField(book.getIsbn());
            TextField titleField = new TextField(book.getTitle());
            TextField authorField = new TextField(book.getAuthorNames()); // Assuming a method to get author names
            ComboBox<Genre> genreComboBox = new ComboBox<>();
            genreComboBox.getItems().addAll(Genre.values());
            genreComboBox.setValue(book.getGenre()); // Assuming book has a getGenre() method
            ComboBox<Integer> ratingComboBox = new ComboBox<>();
            ratingComboBox.getItems().addAll(1, 2, 3, 4, 5);
            ratingComboBox.setValue(book.getRating()); // Assuming book has a getRating() method

            // Add components to the grid
            grid.add(new Label("ISBN:"), 0, 0);
            grid.add(isbnField, 1, 0);
            grid.add(new Label("Title:"), 0, 1);
            grid.add(titleField, 1, 1);
            grid.add(new Label("Author:"), 0, 2);
            grid.add(authorField, 1, 2);
            grid.add(new Label("Genre:"), 0, 3);
            grid.add(genreComboBox, 1, 3);
            grid.add(new Label("Rating:"), 0, 4);
            grid.add(ratingComboBox, 1, 4);

            dialog.getDialogPane().setContent(grid);

            // Convert the result to a Book when the update button is clicked
            dialog.setResultConverter(dialogButton -> {
                if (dialogButton == updateButtonType) {
                    book.setIsbn(isbnField.getText());
                    book.setTitle(titleField.getText());
                    updateAuthorsToString(book, authorField.getText());
                    book.setGenre(genreComboBox.getValue());
                    book.setRating(ratingComboBox.getValue());
                    return book;
                }
                return null;
            });

            return dialog;
        }
    }

