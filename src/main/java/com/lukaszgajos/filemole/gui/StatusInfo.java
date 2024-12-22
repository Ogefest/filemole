package com.lukaszgajos.filemole.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class StatusInfo {

    private static final StringProperty info = new SimpleStringProperty("");

    public static StringProperty infoProperty() {
        return info;
    }

    public static void setMessage(String string) {
        info.set(string);
    }

    public static String getMessage() {
        return info.get();
    }
}
