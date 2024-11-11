package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.AnchorPane;

import java.io.IOException;

public class IndexManagementCell extends ListCell<IndexDefinition> {
    @FXML
    private Button btnRemoveIndex;

    @FXML
    private Label indexName;

    @FXML
    private AnchorPane resultCell;

    @FXML
    private Label labelCount;

    @FXML
    private Label labelSize;

    private FXMLLoader loader;

    private RemoveIndexCallback clbk;
    private IndexDefinition index;

    public IndexManagementCell(RemoveIndexCallback clbk) {
        this.clbk = clbk;
    }

    private void loadFXML() {
        try {
            loader = new FXMLLoader(getClass().getResource("/index-management-cell.fxml"));
            loader.setController(this);
//            loader.setRoot(this);
            loader.load();
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void updateItem(IndexDefinition item, boolean empty) {
        super.updateItem(item, empty);

        if (loader == null) {
            loadFXML();
        }

        if(empty || item == null) {
            setText(null);
            setGraphic(null);
        }
        else {
            index = item;

            indexName.setText(item.getName());
            labelCount.setText("Elements in index: " + index.getItemsCount());
            labelSize.setText("Size: " + formatBytes(index.getItemsSize()));

            btnRemoveIndex.setOnAction(actionEvent -> {
                clbk.remove(item);
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
