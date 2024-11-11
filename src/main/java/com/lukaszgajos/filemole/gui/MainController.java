package com.lukaszgajos.filemole.gui;

import com.lukaszgajos.filemole.domain.entity.Index;
import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import com.lukaszgajos.filemole.domain.entity.Item;
import com.lukaszgajos.filemole.domain.entity.SearchConfiguration;
import com.lukaszgajos.filemole.domain.entity.search.Path;
import com.lukaszgajos.filemole.domain.process.Worker;
import com.lukaszgajos.filemole.domain.service.DatabaseService;
import com.lukaszgajos.filemole.domain.service.IndexService;
import com.lukaszgajos.filemole.domain.service.SearchService;
import com.lukaszgajos.filemole.domain.service.WorkerService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    private LocalDateTime lastSearch = LocalDateTime.now();

    private DatabaseService dbService = new DatabaseService();
    private IndexService indexService;
    private SearchService searchService;
    private WorkerService workerService;

    private ObservableList<Item> searchResults;
    private ObservableList<IndexDefinition> activeIndexDefinitions;


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

        inputSearch.textProperty().addListener((observableValue, s, t1) -> {
            search();
        });

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


    }

    private void search() {

        lastSearch = LocalDateTime.now();

        String search = inputSearch.getText();
        if (search!= null &&!search.isEmpty()) {
            searchResults.clear();

            SearchConfiguration sq = new SearchConfiguration();
            String[] elems = search.split(" ");
            for (String s : elems) {
                if (!s.isEmpty()) {
                    sq.addFilter(new Path(s));
                }
            }

            List<IndexDefinition> searchInIndexes = activeIndexDefinitions.stream()
                    .filter(IndexDefinition::isEnabledForSearch).toList();

            ArrayList<Item> results = new ArrayList<>(searchService.search(searchInIndexes, sq));
            searchResults.addAll(results);

        } else {
            searchResults.clear();
        }

    }
}