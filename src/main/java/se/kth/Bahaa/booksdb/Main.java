package se.kth.Bahaa.booksdb;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import se.kth.Bahaa.booksdb.model.BooksDpImpl;
import se.kth.Bahaa.booksdb.view.BooksPane;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Main extends Application {
    private Properties dbProperties;

    @Override
    public void start(Stage primaryStage) {
        dbProperties = loadDatabaseProperties();
        if (dbProperties == null) {
            System.out.println("Error: Unable to load database configuration.");
            // Optionally, you can stop the application here if the properties are crucial
            return;
        }

        // Initialize your application components with the loaded properties
        BooksDpImpl booksDbImpl = new BooksDpImpl();
        BooksPane booksPane = new BooksPane(booksDbImpl, booksDbImpl, dbProperties);

        // Set up the scene and stage
        Scene scene = new Scene(booksPane, 800, 600); // Adjust the size as needed
        primaryStage.setScene(scene);
        primaryStage.setTitle("Books Database Application");
        primaryStage.show();
    }

    private Properties loadDatabaseProperties() {
        Properties props = new Properties();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.properties")) {
            if (input == null) {
                System.out.println("Sorry, unable to find config.properties");
                return null;
            }
            props.load(input);
            System.out.println("Database URL: " + props.getProperty("db.url"));
            System.out.println("Database User: " + props.getProperty("db.user"));
            System.out.println("Database Password: " + props.getProperty("db.password"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return props;
    }


    public static void main(String[] args) {
        launch(args);
    }
}
