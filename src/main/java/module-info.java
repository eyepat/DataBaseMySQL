module se.kth.anderslm.booksdb {
    requires javafx.controls;
    requires javafx.base;

    opens se.kth.Bahaa.booksdb to javafx.base;
    opens se.kth.Bahaa.booksdb.model to javafx.base; // open model package for reflection from PropertyValuesFactory (sigh ...)
    exports se.kth.Bahaa.booksdb;

    requires java.sql;
    requires javafx.fxml;
}