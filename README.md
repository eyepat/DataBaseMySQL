# Databases-BooksDb-Mock-implementation
This is the views and interface (need to be extended) for assignment 1 and 2. There is no real implementation of the connection to a database server, just a "mock".

# JavaFX and Java SDK, version > 8/1.8
if you are using av JDK with version number > 8/1.8 (which you probably are), you need the following in the module-info.java file (IntelliJ project):

```
module your_base_package_name {
    
    requires java.sql;

    requires javafx.controls;
    requires javafx.base;

    opens your_base_package_name to javafx.base;
    opens your_base_package_name.model to javafx.base; // open model package for reflection from PropertyValuesFactory (sigh ...)
    exports your_base_package_name;
}
```
