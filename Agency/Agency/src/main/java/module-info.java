module org.example.agency {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.xerial.sqlitejdbc;
    requires java.sql;
    requires jbcrypt;
    requires spring.security.crypto;

    opens org.example.agency to javafx.fxml;
    exports org.example.agency;
}