package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListCell;

import java.io.IOException;

public class SearchListIndexCell extends ListCell<IndexDefinition> {
    @FXML
    private CheckBox indexName;
    private IndexDefinition index;

    private FXMLLoader loader;

    private ChangeIndexEnabeldForSearchCallback clbk;

    public SearchListIndexCell(ChangeIndexEnabeldForSearchCallback clbk) {
        this.clbk = clbk;
    }

    private void loadFXML() {
        try {
            loader = new FXMLLoader(getClass().getResource("/search-list-index-cell.fxml"));
            loader.setController(this);
//            loader.setRoot(this);
            loader.load();

            indexName.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
                clbk.changeEnabled(index, indexName.isSelected());
            });
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
            indexName.selectedProperty().set(true);

            setText(null);
            setGraphic(indexName);

        }
    }

    public boolean isChecked() {
        return indexName.isSelected();
    }

    public IndexDefinition getIndexDefinition() {
        return index;
    }
}
