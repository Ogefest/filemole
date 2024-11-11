package com.lukaszgajos.filemole.domain.process;

import com.lukaszgajos.filemole.domain.entity.Item;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

class FilesystemVisitor implements FileVisitor<Path> {

    private ArrayList<Item> visitedItems;

    public FilesystemVisitor() {
        visitedItems = new ArrayList<>();
    }

    public List<Item> getVisitedItems() {
        return visitedItems;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {

        Item item = new Item();
        item.path = file.toString();
        item.name = file.getFileName().toString();
        item.isDir = attrs.isDirectory();
        item.lastModified = attrs.lastModifiedTime().to(TimeUnit.SECONDS);
        item.createdAt = attrs.creationTime().to(TimeUnit.SECONDS);
        item.size = attrs.size();

        if (!item.isDir) {
            item.ext = getFileExtension(item.name);
        }

        visitedItems.add(item);

        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFileFailed(Path file, IOException exc) {
        System.err.println("error on: " + file + " " + exc.getClass());
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}