package com.lukaszgajos.filemole.domain.process;

import com.lukaszgajos.filemole.domain.entity.Item;

import java.io.File;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Filesystem implements Indexer {
    @Override
    public List<Item> getItems(Configuration conf) {
        String start =  (((FilesystemConfiguration)conf).getStartPath());
        return getItemsList(start);
    }

    private static List<Item> getItemsList(String startDirectory) {
        Path startPath = Paths.get(startDirectory);
        FilesystemVisitor visitor = new FilesystemVisitor();
        try {
            Files.walkFileTree(startPath, visitor);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return visitor.getVisitedItems();
    }
}
