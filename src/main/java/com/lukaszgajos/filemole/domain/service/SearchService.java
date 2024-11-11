package com.lukaszgajos.filemole.domain.service;

import com.lukaszgajos.filemole.domain.entity.IndexDefinition;
import com.lukaszgajos.filemole.domain.entity.Item;
import com.lukaszgajos.filemole.domain.entity.SearchConfiguration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SearchService {

    private DatabaseService db;

    public SearchService(DatabaseService db) {
        this.db = db;
    }
    public ArrayList<Item> search(List<IndexDefinition> searchIndexDefinitions, SearchConfiguration conf) {

        AtomicReference<String> filters = new AtomicReference<>("");
        conf.getFilters().forEach(filter -> {
            filters.set(filters + " AND (" + filter.getQuery() + ") ");
        });
        StringBuilder searchSql = new StringBuilder("""
                SELECT * FROM index_item WHERE (
                """);
        for (IndexDefinition idx : searchIndexDefinitions) {
            searchSql.append(" index_id = ").append(idx.getId()).append(" OR ");
        }
        searchSql = new StringBuilder(searchSql.substring(0, searchSql.length() - 3));
        searchSql.append(") ");
        searchSql.append(" ").append(filters).append(" LIMIT 1000");

        ArrayList<Item> items = new ArrayList<>();
        try {
            Statement stmt = db.getConnection().createStatement();
            ResultSet rs = stmt.executeQuery(String.valueOf(searchSql.toString()));

            while (rs.next()) {
                Item item = new Item();
                item.path = rs.getString("path");
                item.name = rs.getString("name");
                item.size = rs.getLong("size");
//                item.ext = rs.getString("ext");
//                item.isDir = rs.getBoolean("isDir");
                items.add(item);
            }
            stmt.close();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        return items;
    }
}
