package org.example.dao.postgres;

import org.example.database.DatabaseConnection;
import org.example.model.Card;
import org.example.dao.CardDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresCardDao implements CardDao {

    @Override
    public Card create(Card card) {
        String sql = "INSERT INTO cards (pan, cvv, date_of_expire, name, surname, currency, balance) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?) RETURNING card_id";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, card.getPan());
            pstmt.setString(2, card.getCvv());
            pstmt.setString(3, card.getDateOfExpire());
            pstmt.setString(4, card.getName());
            pstmt.setString(5, card.getSurname());
            pstmt.setString(6, card.getCurrency());
            pstmt.setDouble(7, card.getBalance());
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                card.setCardId(rs.getInt("card_id"));
            }
            
            return card;
        } catch (SQLException e) {
            System.err.println("Error creating card: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<Card> findById(Integer cardId) {
        String sql = "SELECT * FROM cards WHERE card_id = ?";
        return findCardByQuery(sql, cardId);
    }

    @Override
    public Optional<Card> findByPan(String pan) {
        String sql = "SELECT * FROM cards WHERE pan = ?";
        return findCardByQuery(sql, pan);
    }

    private Optional<Card> findCardByQuery(String sql, Object param) {
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            if (param instanceof Integer) {
                pstmt.setInt(1, (Integer) param);
            } else {
                pstmt.setString(1, (String) param);
            }
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Optional.of(mapResultSetToCard(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding card: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<Card> findByNameAndSurname(String name, String surname) {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards WHERE name = ? AND surname = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, surname);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                cards.add(mapResultSetToCard(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding cards by name: " + e.getMessage());
        }
        return cards;
    }

    @Override
    public List<Card> findAll() {
        List<Card> cards = new ArrayList<>();
        String sql = "SELECT * FROM cards";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                cards.add(mapResultSetToCard(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all cards: " + e.getMessage());
        }
        return cards;
    }

    @Override
    public boolean update(Card card) {
        String sql = "UPDATE cards SET pan = ?, cvv = ?, date_of_expire = ?, name = ?, " +
                     "surname = ?, currency = ?, balance = ? WHERE card_id = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, card.getPan());
            pstmt.setString(2, card.getCvv());
            pstmt.setString(3, card.getDateOfExpire());
            pstmt.setString(4, card.getName());
            pstmt.setString(5, card.getSurname());
            pstmt.setString(6, card.getCurrency());
            pstmt.setDouble(7, card.getBalance());
            pstmt.setInt(8, card.getCardId());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating card: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(Integer cardId) {
        String sql = "DELETE FROM cards WHERE card_id = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, cardId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting card: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByPan(String pan) {
        String sql = "DELETE FROM cards WHERE pan = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pan);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting card: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsByPan(String pan) {
        String sql = "SELECT 1 FROM cards WHERE pan = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, pan);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking card existence: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean transferMoney(String fromPan, String toPan, Double amount) {
        try (Connection conn = DatabaseConnection.getPostgresConnection()) {
            conn.setAutoCommit(false);
            
            try {
                String withdrawSql = "UPDATE cards SET balance = balance - ? WHERE pan = ? AND balance >= ?";
                try (PreparedStatement pstmt = conn.prepareStatement(withdrawSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setString(2, fromPan);
                    pstmt.setDouble(3, amount);
                    if (pstmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                
                String depositSql = "UPDATE cards SET balance = balance + ? WHERE pan = ?";
                try (PreparedStatement pstmt = conn.prepareStatement(depositSql)) {
                    pstmt.setDouble(1, amount);
                    pstmt.setString(2, toPan);
                    if (pstmt.executeUpdate() == 0) {
                        conn.rollback();
                        return false;
                    }
                }
                
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println("Error transferring money: " + e.getMessage());
            return false;
        }
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
        String sql = "UPDATE cards SET balance = balance - ? WHERE pan = ? AND cvv = ? AND balance >= ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setString(2, pan);
            pstmt.setString(3, cvv);
            pstmt.setDouble(4, amount);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error withdrawing money: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean depositMoney(String pan, Double amount) {
        String sql = "UPDATE cards SET balance = balance + ? WHERE pan = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDouble(1, amount);
            pstmt.setString(2, pan);
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error depositing money: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean depositMoneyByName(String name, String surname, Double amount) {
        List<Card> cards = findByNameAndSurname(name, surname);
        if (cards.isEmpty()) {
            return false;
        }
        return depositMoney(cards.get(0).getPan(), amount);
    }

    private Card mapResultSetToCard(ResultSet rs) throws SQLException {
        Card card = new Card();
        card.setCardId(rs.getInt("card_id"));
        card.setPan(rs.getString("pan"));
        card.setCvv(rs.getString("cvv"));
        card.setDateOfExpire(rs.getString("date_of_expire"));
        card.setName(rs.getString("name"));
        card.setSurname(rs.getString("surname"));
        card.setCurrency(rs.getString("currency"));
        card.setBalance(rs.getDouble("balance"));
        return card;
    }
}

