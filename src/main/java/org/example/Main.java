package org.example;

import org.example.database.DatabaseConnection;
import org.example.model.Card;
import org.example.model.CardUser;
import org.example.dao.CardDao;
import org.example.dao.CardUserDao;
import org.example.dao.mongo.MongoCardDao;
import org.example.dao.mongo.MongoCardUserDao;
import org.example.dao.postgres.PostgresCardDao;
import org.example.dao.postgres.PostgresCardUserDao;

public class Main {
    
    private CardUserDao cardUserDao;
    private CardDao cardDao;
    private String currentDatabase;
    
    public Main(String databaseType) {
        switchDatabase(databaseType);
    }
    
    public void switchDatabase(String databaseType) {
        if ("postgres".equalsIgnoreCase(databaseType)) {
            this.cardUserDao = new PostgresCardUserDao();
            this.cardDao = new PostgresCardDao();
            this.currentDatabase = "PostgreSQL";
            System.out.println("Switched to PostgreSQL");
        } else if ("mongo".equalsIgnoreCase(databaseType) || "mongodb".equalsIgnoreCase(databaseType)) {
            this.cardUserDao = new MongoCardUserDao();
            this.cardDao = new MongoCardDao();
            this.currentDatabase = "MongoDB";
            System.out.println("Switched to MongoDB");
        } else {
            throw new IllegalArgumentException("Unknown database type: " + databaseType);
        }
    }
    
    public CardUserDao getCardUserDao() {
        return cardUserDao;
    }
    
    public CardDao getCardDao() {
        return cardDao;
    }
    
    public String getCurrentDatabase() {
        return currentDatabase;
    }
    
    public static void main(String[] args) {
        System.out.println("Initializing PostgreSQL database...");
        DatabaseConnection.initializePostgresDatabase();
        
        System.out.println("\n=== Using PostgreSQL ===");
        Main app = new Main("postgres");
        demonstrateOperations(app);
        
        System.out.println("\n=== Switching to MongoDB ===");
        app.switchDatabase("mongo");
        demonstrateOperations(app);
    }
    
    private static void demonstrateOperations(Main app) {
        CardUserDao userDao = app.getCardUserDao();
        CardDao cardDao = app.getCardDao();
        
        CardUser user = new CardUser("Иван", "Иванов", "123456789012");
        user = userDao.create(user);
        System.out.println("Created user: " + user);
        
        Card card = new Card(null, "1234567890123456", Card.generateCVV(), 
                           Card.generateExpireDate(), user.getName(), user.getSurname(), 
                           "KZT", 1000.50);
        card = cardDao.create(card);
        System.out.println("Created card: " + card);
        
        System.out.println("\nAll cards:");
        cardDao.findAll().forEach(c -> System.out.println(c.getFullName() + " = " + c.getPan() + ", " + c.getCvv()));
    }
}
