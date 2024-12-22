package com.lukaszgajos.filemole.domain.service;

import com.lukaszgajos.filemole.domain.entity.Index;
import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import com.lukaszgajos.filemole.domain.entity.Item;
import com.lukaszgajos.filemole.domain.process.Filesystem;
import com.lukaszgajos.filemole.domain.process.FilesystemConfiguration;
import com.lukaszgajos.filemole.gui.StatusInfo;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class IndexService {

    private DatabaseService db;

    public IndexService(DatabaseService db) {
        this.db = db;
    }

    public ArrayList<IndexDefinition> getActiveIndexes() {
        try (Statement stmt = db.getConnection().createStatement()) {
            String activeIndexesSql = "SELECT id,name,size,elements FROM index_definition WHERE is_active = 1";
            ResultSet rs = stmt.executeQuery(activeIndexesSql);
            ArrayList<IndexDefinition> indexDefinitions = new ArrayList<>();
            while (rs.next()) {
                String name = rs.getString("name");
                int id = rs.getInt("id");
                long size = rs.getLong("size");
                long elements = rs.getLong("elements");
                IndexDefinition indexDefinition = new IndexDefinition(id, name, elements, size);
                indexDefinitions.add(indexDefinition);
            }
            return indexDefinitions;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public IndexDefinition getIndexByName(String name) {
        ArrayList<IndexDefinition> activeIndexDefinitions = getActiveIndexes();
        for (IndexDefinition indexDefinition : activeIndexDefinitions) {
            if (indexDefinition.getName().equals(name)) {
                return indexDefinition;
            }
        }
        return null;
    }

    public void saveIndex(Index index) {

        StatusInfo.setMessage("Saving index to db");

        IndexDefinition indexDefinition = index.getDefinition();
        long sizeInBytes = 0;
        long itemsCount = index.getItems().size();
        for (Item it : index.getItems()) {
            sizeInBytes += it.size;
        }

        String insertSQL = "INSERT INTO index_definition (name, is_active, size, elements, created_at,last_updated,update_interval) VALUES (?,?,?,?,?,?,?)";
        String query = "SELECT last_insert_rowid()";

        try (PreparedStatement pstmt = db.getConnection().prepareStatement(insertSQL)) {
            pstmt.setString(1, indexDefinition.getName());
            pstmt.setString(2, "1");
            pstmt.setLong(3, sizeInBytes);
            pstmt.setLong(4, itemsCount);
            pstmt.setString(5, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));;
            pstmt.setString(6, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));;
            pstmt.setString(7, LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));;

            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        long lastInsertId = 0;

        try (PreparedStatement stmt = db.getConnection().prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                lastInsertId = rs.getLong(1);  // Get the last inserted ID
                System.out.println("Last Inserted ID: " + lastInsertId);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        long finalLastInsertId = lastInsertId;
        long savedCounter = 0;
        String insert = "INSERT INTO index_item (index_id, path, name, size, ext, is_dir, last_modified, created_at, archive) VALUES (?,?,?,?,?,?,?,?,?)";
        try (PreparedStatement pstmt = db.getConnection().prepareStatement(insert)) {
            db.getConnection().setAutoCommit(false);


            for (Item item : index.getItems()) {
                savedCounter++;

                pstmt.setLong(1, finalLastInsertId);
                pstmt.setString(2, item.path);
                pstmt.setString(3, item.name);
                pstmt.setLong(4, item.size);
                pstmt.setString(5, item.ext);
                pstmt.setString(6, item.isDir ? "1" : "0");
                pstmt.setLong(7, item.lastModified);
                pstmt.setLong(8, item.createdAt);
                pstmt.setString(9, item.archive);
                pstmt.addBatch();

                if (savedCounter % 1000 == 0) {
                    StatusInfo.setMessage("Saving index to db " + savedCounter + " files");
                }
            }
            System.out.println("Save batch finished");

            pstmt.executeBatch();
            db.getConnection().commit();
            db.getConnection().setAutoCommit(true);
            pstmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Index saved");

        StatusInfo.setMessage("");
    }

    public Index buildNewIndex(String path) {
        IndexDefinition indexDefinition = new IndexDefinition(0, path,0,0);
        FilesystemConfiguration fsconf = new FilesystemConfiguration(path);
        Filesystem fs = new Filesystem();
        StatusInfo.setMessage("Start indexing for path " + path);
        Index newIndex = new Index(indexDefinition, fs.getItems(fsconf));

        return newIndex;
    }

    public void removeIndex(IndexDefinition index) {

        String sql1 = "DELETE FROM index_definition WHERE id = ?";
        String sql2 = "DELETE FROM index_item WHERE index_id = ?";
        String sql3 = "VACUUM";

        try {
            PreparedStatement pstmt1 = db.getConnection().prepareStatement(sql1);
            pstmt1.setInt(1, index.getId());
            pstmt1.execute();
            pstmt1.close();

            PreparedStatement pstmt2 = db.getConnection().prepareStatement(sql2);
            pstmt2.setInt(1, index.getId());
            pstmt2.execute();
            pstmt2.close();

            Statement stmt = db.getConnection().createStatement();
            stmt.execute(sql3);
            stmt.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


    }

}
