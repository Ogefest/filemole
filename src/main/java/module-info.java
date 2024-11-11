module com.lukaszgajos.filemole {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.xerial.sqlitejdbc;
    requires java.desktop;

    opens com.lukaszgajos.filemole to javafx.fxml;
    exports com.lukaszgajos.filemole;
    exports com.lukaszgajos.filemole.gui;
    opens com.lukaszgajos.filemole.gui to javafx.fxml;
}