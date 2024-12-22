package com.lukaszgajos.filemole.domain.process;

import com.lukaszgajos.filemole.domain.entity.Item;
import com.lukaszgajos.filemole.gui.StatusInfo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

class FilesystemVisitor implements FileVisitor<Path> {

    private ArrayList<Item> visitedItems;
    private long lastStatus = 0;

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

        if (lastStatus + 1000 < System.currentTimeMillis()) {
            lastStatus = System.currentTimeMillis();
            StatusInfo.setMessage("Indexing " + file.toFile().getAbsolutePath());
        }

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

        if (item.ext.equals("zip")) {
            try {
                ArrayList<Item> zipItems = processZipFile(file);
                visitedItems.addAll(zipItems);
            } catch (IOException e) {
                System.err.printf("Unable to process %s because %s\n", file.toFile().getAbsolutePath(), e.getMessage());
            }
        }

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

    private ArrayList<Item> processZipFile(Path zipFilePath) throws IOException {
        ArrayList<Item> items = new ArrayList<>();

        try (ZipFile zipFile = new ZipFile(zipFilePath.toFile())) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                Item item = new Item();
                item.path = entry.getName();
                item.name = Paths.get(entry.getName()).getFileName().toString();
                item.isDir = entry.isDirectory();
                item.lastModified = entry.getLastModifiedTime() != null ? entry.getLastModifiedTime().to(TimeUnit.SECONDS) : 0;
                item.createdAt = entry.getCreationTime() != null ? entry.getCreationTime().to(TimeUnit.SECONDS) : 0;
                item.size = entry.isDirectory() ? 0 : entry.getSize();
                item.archive = zipFilePath.toFile().getAbsolutePath();

                if (!item.isDir) {
                    item.ext = getFileExtension(item.name);
                }
                items.add(item);
            }
        }

        return items;
    }

    private static String getFileExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf('.');
        return (dotIndex == -1) ? "" : fileName.substring(dotIndex + 1);
    }
}