package org.example.database;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnection {
    private static final String DEFAULT_CONNECTION_STRING = "mongodb+srv://mangodbswiz:123@cluster0.guocwkk.mongodb.net/?appName=Cluster0";
    private static final String DATABASE_NAME = "banking_system";
    
    private static String connectionString = DEFAULT_CONNECTION_STRING;
    
    public static void setConnectionString(String connectionString) {
        MongoConnection.connectionString = connectionString;
    }
    
    public static MongoDatabase getDatabase() {
        MongoClient mongoClient = MongoClients.create(connectionString);
        return mongoClient.getDatabase(DATABASE_NAME);
    }
    
    public static MongoClient getClient() {
        return MongoClients.create(connectionString);
    }
}
