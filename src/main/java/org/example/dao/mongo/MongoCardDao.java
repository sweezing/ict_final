package org.example.dao.mongo;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.example.database.MongoConnection;
import org.example.model.Card;
import org.example.dao.CardDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MongoCardDao implements CardDao {
    
    private MongoCollection<Document> getCollection() {
        MongoDatabase database = MongoConnection.getDatabase();
        return database.getCollection("cards");
    }

    @Override
    public Card create(Card card) {
        Document doc = new Document()
            .append("pan", card.getPan())
            .append("cvv", card.getCvv())
            .append("dateOfExpire", card.getDateOfExpire())
            .append("name", card.getName())
            .append("surname", card.getSurname())
            .append("currency", card.getCurrency())
            .append("balance", card.getBalance());
        
        getCollection().insertOne(doc);
        
        if (doc.getObjectId("_id") != null) {
            card.setCardId(doc.getObjectId("_id").hashCode());
        }
        
        return card;
    }

    @Override
    public Optional<Card> findById(Integer cardId) {
        return Optional.empty();
    }

    @Override
    public Optional<Card> findByPan(String pan) {
        Document doc = getCollection().find(Filters.eq("pan", pan)).first();
        if (doc != null) {
            return Optional.of(mapDocumentToCard(doc));
        }
        return Optional.empty();
    }

    @Override
    public List<Card> findByNameAndSurname(String name, String surname) {
        List<Card> cards = new ArrayList<>();
        for (Document doc : getCollection().find(
            Filters.and(
                Filters.eq("name", name),
                Filters.eq("surname", surname)
            )
        )) {
            cards.add(mapDocumentToCard(doc));
        }
        return cards;
    }

    @Override
    public List<Card> findAll() {
        List<Card> cards = new ArrayList<>();
        for (Document doc : getCollection().find()) {
            cards.add(mapDocumentToCard(doc));
        }
        return cards;
    }

    @Override
    public boolean update(Card card) {
        return getCollection().updateOne(
            Filters.eq("pan", card.getPan()),
            Updates.combine(
                Updates.set("cvv", card.getCvv()),
                Updates.set("dateOfExpire", card.getDateOfExpire()),
                Updates.set("name", card.getName()),
                Updates.set("surname", card.getSurname()),
                Updates.set("currency", card.getCurrency()),
                Updates.set("balance", card.getBalance())
            )
        ).getModifiedCount() > 0;
    }

    @Override
    public boolean deleteById(Integer cardId) {
        return false;
    }

    @Override
    public boolean deleteByPan(String pan) {
        return getCollection().deleteOne(Filters.eq("pan", pan)).getDeletedCount() > 0;
    }

    @Override
    public boolean existsByPan(String pan) {
        return getCollection().countDocuments(Filters.eq("pan", pan)) > 0;
    }

    @Override
    public boolean transferMoney(String fromPan, String toPan, Double amount) {
        Optional<Card> fromCardOpt = findByPan(fromPan);
        Optional<Card> toCardOpt = findByPan(toPan);
        
        if (fromCardOpt.isEmpty() || toCardOpt.isEmpty()) {
            return false;
        }
        
        Card fromCard = fromCardOpt.get();
        Card toCard = toCardOpt.get();
        
        if (fromCard.getBalance() < amount) {
            return false;
        }
        
        boolean withdrawSuccess = getCollection().updateOne(
            Filters.and(
                Filters.eq("pan", fromPan),
                Filters.gte("balance", amount)
            ),
            Updates.inc("balance", -amount)
        ).getModifiedCount() > 0;
        
        if (!withdrawSuccess) {
            return false;
        }
        
        boolean depositSuccess = getCollection().updateOne(
            Filters.eq("pan", toPan),
            Updates.inc("balance", amount)
        ).getModifiedCount() > 0;
        
        return depositSuccess;
    }

    @Override
    public boolean transferMoneyByName(String fromName, String fromSurname, 
                                       String toName, String toSurname, Double amount) {
        List<Card> fromCards = findByNameAndSurname(fromName, fromSurname);
        List<Card> toCards = findByNameAndSurname(toName, toSurname);
        
        if (fromCards.isEmpty() || toCards.isEmpty()) {
            return false;
        }
        
        return transferMoney(fromCards.get(0).getPan(), toCards.get(0).getPan(), amount);
    }

    @Override
    public boolean withdrawMoney(String pan, String cvv, Double amount) {
        return getCollection().updateOne(
            Filters.and(
                Filters.eq("pan", pan),
                Filters.eq("cvv", cvv),
                Filters.gte("balance", amount)
            ),
            Updates.inc("balance", -amount)
        ).getModifiedCount() > 0;
    }

    @Override
    public boolean depositMoney(String pan, Double amount) {
        return getCollection().updateOne(
            Filters.eq("pan", pan),
            Updates.inc("balance", amount)
        ).getModifiedCount() > 0;
    }

    @Override
    public boolean depositMoneyByName(String name, String surname, Double amount) {
        List<Card> cards = findByNameAndSurname(name, surname);
        if (cards.isEmpty()) {
            return false;
        }
        return depositMoney(cards.get(0).getPan(), amount);
    }

    private Card mapDocumentToCard(Document doc) {
        Card card = new Card();
        if (doc.getObjectId("_id") != null) {
            card.setCardId(doc.getObjectId("_id").hashCode());
        }
        card.setPan(doc.getString("pan"));
        card.setCvv(doc.getString("cvv"));
        card.setDateOfExpire(doc.getString("dateOfExpire"));
        card.setName(doc.getString("name"));
        card.setSurname(doc.getString("surname"));
        card.setCurrency(doc.getString("currency"));
        card.setBalance(doc.getDouble("balance"));
        return card;
    }
}

