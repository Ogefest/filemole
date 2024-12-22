package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import com.lukaszgajos.filemole.domain.entity.Item;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.AnchorPane;

import java.awt.*;
import java.io.File;
import java.io.IOException;

public class SearchResultCell extends ListCell<Item> {
    @FXML
    private Label itemName;

    @FXML
    private Label itemPath;

    @FXML
    private AnchorPane resultCell;

    @FXML
    private Label labelSize;

    @FXML
    private Button btnCopyPath;

    @FXML
    private Button btnOpenDir;

    private Item item;

    private FXMLLoader loader;


    private void loadFXML() {
        try {
            loader = new FXMLLoader(getClass().getResource("/search-result-cell.fxml"));
            loader.setController(this);
//            loader.setRoot(this);
            loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);

        if (loader == null) {
            loadFXML();
        }

        if(empty || item == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            this.item = item;

            if (item.archive != null) {
                item.path = item.archive;
            }

            itemName.setText(item.name);
            itemPath.setText(item.path);

            labelSize.setText(formatBytes(item.size));
            btnCopyPath.setOnAction(actionEvent -> {
                Clipboard clipboard = Clipboard.getSystemClipboard();
                ClipboardContent content = new ClipboardContent();
                content.putString(item.path);
                clipboard.setContent(content);
            });
            btnOpenDir.setOnAction(actionEvent -> {
                File file = new File(item.path);
                if (file != null && Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().open(file.getParentFile()); // Opens the directory
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            });

            setText(null);
            setGraphic(resultCell);

        }
    }

    private String formatBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;

        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return String.format("%.2f %s", size, units[unitIndex]);
    }
}
