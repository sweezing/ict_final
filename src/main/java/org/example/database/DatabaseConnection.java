package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String POSTGRES_URL = "jdbc:postgresql://localhost:5432/banking_system";
    private static final String POSTGRES_USER = "postgres";
    private static final String POSTGRES_PASSWORD = "";

    public static Connection getPostgresConnection() throws SQLException {
        return DriverManager.getConnection(POSTGRES_URL, POSTGRES_USER, POSTGRES_PASSWORD);
    }

    public static void initializePostgresDatabase() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres", POSTGRES_USER, POSTGRES_PASSWORD)) {
            
            conn.createStatement().executeUpdate(
                "SELECT 1 FROM pg_database WHERE datname = 'banking_system'"
            );
            
            try {
                conn.createStatement().executeUpdate("CREATE DATABASE banking_system");
            } catch (SQLException e) {
            }
        } catch (SQLException e) {
            System.err.println("Error initializing database: " + e.getMessage());
        }

        try (Connection conn = getPostgresConnection()) {
            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS card_users (" +
                "name VARCHAR(100) NOT NULL, " +
                "surname VARCHAR(100) NOT NULL, " +
                "iin VARCHAR(20) PRIMARY KEY" +
                ")"
            );

            conn.createStatement().executeUpdate(
                "CREATE TABLE IF NOT EXISTS cards (" +
                "card_id SERIAL PRIMARY KEY, " +
                "pan VARCHAR(16) UNIQUE NOT NULL, " +
                "cvv VARCHAR(3) NOT NULL, " +
                "date_of_expire VARCHAR(5) NOT NULL, " +
                "name VARCHAR(100) NOT NULL, " +
                "surname VARCHAR(100) NOT NULL, " +
                "currency VARCHAR(10), " +
                "balance DECIMAL(15, 2) NOT NULL" +
                ")"
            );

            System.out.println("PostgreSQL database initialized successfully");
        } catch (SQLException e) {
            System.err.println("Error creating tables: " + e.getMessage());
        }
    }
}
