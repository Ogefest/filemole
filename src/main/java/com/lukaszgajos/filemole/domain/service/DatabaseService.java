package com.lukaszgajos.filemole.domain.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseService {
    private static final String URL = "jdbc:sqlite:files.db";
    private Connection conn;

    public DatabaseService() {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            conn = DriverManager.getConnection(URL);
            createInitTables();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private void createInitTables() throws SQLException {
        Statement stmt = conn.createStatement();
        String createTableSQL = """
                CREATE TABLE IF NOT EXISTS index_item (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    index_id INTEGER,
                    name TEXT NOT NULL,
                    path TEXT NOT NULL,
                    archive TEXT NULL,
                    size LONG NOT NULL,
                    is_dir TEXT NOT NULL,
                    ext TEXT NULL DEFAULT '',
                    last_modified LONG DEFAULT '',
                    created_at LONG DEFAULT '',
                    FOREIGN KEY (index_id) REFERENCES index_table(id)
                );
                """;
        String createIndexDefinitionTableSQL = """
                CREATE TABLE IF NOT EXISTS index_definition (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    is_active INT NOT NULL,
                    size LONG NOT NULL,
                    elements LONG NOT NULL,
                    created_at TEXT NOT NULL,
                    last_updated TEXT NOT NULL,
                    update_interval INT NOT NULL
                );
            """;
        String createIndex = """
                CREATE INDEX IF NOT EXISTS idx_index_item_index_id ON index_item (index_id);
                """;

        stmt.execute(createTableSQL);
        stmt.execute(createIndexDefinitionTableSQL);
        stmt.execute(createIndex);
        stmt.close();
    }


    public Connection getConnection() {
        return conn;
    }

    public void executeSql(String sql) {
        try (Statement stmt = conn.createStatement()) {
            stmt.executeQuery(sql);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
