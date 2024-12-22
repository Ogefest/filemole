package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.Index;
import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import com.lukaszgajos.filemole.domain.entity.Item;
import com.lukaszgajos.filemole.domain.entity.SearchConfiguration;
import com.lukaszgajos.filemole.domain.entity.search.*;
import com.lukaszgajos.filemole.domain.process.Worker;
import com.lukaszgajos.filemole.domain.service.DatabaseService;
import com.lukaszgajos.filemole.domain.service.IndexService;
import com.lukaszgajos.filemole.domain.service.SearchService;
import com.lukaszgajos.filemole.domain.service.WorkerService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.DirectoryChooser;
import javafx.scene.control.Label;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MainController {
    @FXML
    private Button btnSearch;

    @FXML
    private TextField inputStartPath;

    @FXML
    private Button btnAddIndex;

    @FXML
    private Button btnSelectDirectory;

    @FXML
    private ListView<IndexDefinition> listViewIndexManagement;

    @FXML
    private ListView<IndexDefinition> listViewActiveIndexes;

    @FXML
    private ListView<Item> listViewSearchResults;

    @FXML
    private TextField inputSearch;

    @FXML
    private ToggleButton btnAdvancedFilters;
    @FXML
    private TextField inputExcludePhrase;

    @FXML
    private TextField inputExtensions;

    @FXML
    private TextField inputMaxSize;

    @FXML
    private TextField inputMinSize;

    @FXML
    private AnchorPane paneAdvancedFilters;

    @FXML
    private AnchorPane paneSearchResult;

    @FXML
    private ComboBox<String> comboSize;

    @FXML
    private Label labelStatusMessage;

    private LocalDateTime lastSearch = LocalDateTime.now();

    private DatabaseService dbService = new DatabaseService();
    private IndexService indexService;
    private SearchService searchService;
    private WorkerService workerService;

    private ObservableList<Item> searchResults;
    private ObservableList<IndexDefinition> activeIndexDefinitions;
    private StringProperty statusMessage;


    @FXML
    public void initialize() {
        indexService = new IndexService(dbService);
        searchService = new SearchService(dbService);
        workerService = new WorkerService();

        activeIndexDefinitions = FXCollections.observableArrayList(indexService.getActiveIndexes());
        searchResults = FXCollections.observableArrayList(new ArrayList<>());

        listViewActiveIndexes.itemsProperty().set(activeIndexDefinitions);
        listViewSearchResults.itemsProperty().set(searchResults);
        listViewIndexManagement.itemsProperty().set(activeIndexDefinitions);

        btnSearch.setOnAction(event -> {
            search();
        });

        inputSearch.textProperty().addListener(this::onSearchInputChange);
        inputExcludePhrase.textProperty().addListener(this::onSearchInputChange);
        inputMaxSize.textProperty().addListener(this::onSearchInputChange);
        inputMinSize.textProperty().addListener(this::onSearchInputChange);
        inputExtensions.textProperty().addListener(this::onSearchInputChange);

        btnAddIndex.setOnAction(actionEvent -> {
            String startPath = inputStartPath.getText();

            workerService.registerNewJob(new Worker(() -> {
                Index newIndexDefinition = indexService.buildNewIndex(startPath);
                indexService.saveIndex(newIndexDefinition);

                Platform.runLater(() -> {
                    activeIndexDefinitions.setAll(indexService.getActiveIndexes());
                });
            }));
            inputStartPath.setText("");
        });

        listViewActiveIndexes.setCellFactory(indexListView -> new SearchListIndexCell((index, state) -> {
            activeIndexDefinitions.forEach(indexDefinition -> {
                if (indexDefinition.getId() == index.getId()) {
                    indexDefinition.setEnabledForSearch(state);
                }
            });
            search();
        }));
        listViewSearchResults.setCellFactory(itemListView -> new SearchResultCell());
        listViewIndexManagement.setCellFactory(indexDefinitionListView -> new IndexManagementCell((indexDefinition) -> {
            indexService.removeIndex(indexDefinition);

            activeIndexDefinitions.clear();
            activeIndexDefinitions.setAll(indexService.getActiveIndexes());
        }));

        btnSelectDirectory.setOnAction(actionEvent -> {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            File selectedDir = directoryChooser.showDialog(btnSelectDirectory.getScene().getWindow());
            if (selectedDir != null) {
                inputStartPath.setText(selectedDir.getAbsolutePath());
            }
        });
        listViewSearchResults.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String filePath = listViewSearchResults.getSelectionModel().getSelectedItem().path;
                File file = new File(filePath);
                if (file != null && Desktop.isDesktopSupported()) {
                    new Thread(() -> {
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });

        paneAdvancedFilters.setVisible(false);
        paneAdvancedFilters.setManaged(false);
        AnchorPane.setTopAnchor(paneSearchResult, 60.0);
        btnAdvancedFilters.setOnAction(actionEvent -> {
            boolean isFiltersActive = btnAdvancedFilters.isSelected();
            paneAdvancedFilters.setVisible(isFiltersActive);
            paneAdvancedFilters.setManaged(isFiltersActive);
            if (isFiltersActive) {
                AnchorPane.setTopAnchor(paneSearchResult, 180.0);
            } else {
                AnchorPane.setTopAnchor(paneSearchResult, 60.0);
                inputMaxSize.setText("");
                inputMinSize.setText("");
                inputExtensions.setText("");
                inputExcludePhrase.setText("");
            }
        });
        comboSize.getItems().addAll("B", "KB", "MB", "GB", "TB");
        comboSize.setValue("MB");

        statusMessage = new SimpleStringProperty();
        statusMessage.bind(StatusInfo.infoProperty());
        statusMessage.addListener((observableValue, s, t1) -> {
            Platform.runLater(() -> {
                labelStatusMessage.setText(statusMessage.getValue());
            });
        });

        Platform.runLater(() -> {
            inputSearch.requestFocus();
        });

    }

    private void onSearchInputChange(ObservableValue<? extends String> observableValue, String oldValue, String newValue) {
        search();
    }

    private void search() {

        lastSearch = LocalDateTime.now();

        String search = inputSearch.getText();
        if (search!= null &&!search.isEmpty()) {
            searchResults.clear();

            SearchConfiguration sq = new SearchConfiguration();
            sq.addFilter(new Path(search));
            if (!inputExcludePhrase.getText().isEmpty()) {
                sq.addFilter(new Exclude(inputExcludePhrase.getText()));
            }
            if (!inputMinSize.getText().isEmpty()) {
                sq.addFilter(new MinSize(inputMinSize.getText(), comboSize.getValue()));
            }
            if (!inputMaxSize.getText().isEmpty()) {
                sq.addFilter(new MaxSize(inputMaxSize.getText(), comboSize.getValue()));
            }
            if (!inputExtensions.getText().isEmpty()) {
                sq.addFilter(new Extension(inputExtensions.getText()));
            }


            List<IndexDefinition> searchInIndexes = activeIndexDefinitions.stream()
                    .filter(IndexDefinition::isEnabledForSearch).toList();

            if (!searchInIndexes.isEmpty()) {
                ArrayList<Item> results = new ArrayList<>(searchService.search(searchInIndexes, sq));
                searchResults.addAll(results);
            }

        } else {
            searchResults.clear();
        }

    }
}