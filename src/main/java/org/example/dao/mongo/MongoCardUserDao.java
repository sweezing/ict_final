package org.example.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.example.database.MongoConnection;
import org.example.model.CardUser;
import org.example.dao.CardUserDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoCardUserDao implements CardUserDao {
    
    private MongoCollection<Document> getCollection() {
        MongoDatabase database = MongoConnection.getDatabase();
        return database.getCollection("card_users");
    }

    @Override
    public CardUser create(CardUser cardUser) {
        Document doc = new Document()
            .append("name", cardUser.getName())
            .append("surname", cardUser.getSurname())
            .append("iin", cardUser.getIin());
        
        getCollection().insertOne(doc);
        return cardUser;
    }

    @Override
    public Optional<CardUser> findByIin(String iin) {
        Document doc = getCollection().find(Filters.eq("iin", iin)).first();
        if (doc != null) {
            return Optional.of(mapDocumentToCardUser(doc));
        }
        return Optional.empty();
    }

    @Override
    public Optional<CardUser> findByNameAndSurname(String name, String surname) {
        Document doc = getCollection().find(
            Filters.and(
                Filters.eq("name", name),
                Filters.eq("surname", surname)
            )
        ).first();
        
        if (doc != null) {
            return Optional.of(mapDocumentToCardUser(doc));
        }
        return Optional.empty();
    }

    @Override
    public List<CardUser> findAll() {
        List<CardUser> users = new ArrayList<>();
        for (Document doc : getCollection().find()) {
            users.add(mapDocumentToCardUser(doc));
        }
        return users;
    }

    @Override
    public boolean update(CardUser cardUser) {
        return getCollection().updateOne(
            Filters.eq("iin", cardUser.getIin()),
            Updates.combine(
                Updates.set("name", cardUser.getName()),
                Updates.set("surname", cardUser.getSurname())
            )
        ).getModifiedCount() > 0;
    }

    @Override
    public boolean deleteByIin(String iin) {
        return getCollection().deleteOne(Filters.eq("iin", iin)).getDeletedCount() > 0;
    }

    @Override
    public boolean existsByIin(String iin) {
        return getCollection().countDocuments(Filters.eq("iin", iin)) > 0;
    }

    private CardUser mapDocumentToCardUser(Document doc) {
        return new CardUser(
            doc.getString("name"),
            doc.getString("surname"),
            doc.getString("iin")
        );
    }
}

