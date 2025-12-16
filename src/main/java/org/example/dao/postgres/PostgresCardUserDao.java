package org.example.dao.postgres;

import org.example.database.DatabaseConnection;
import org.example.model.CardUser;
import org.example.dao.CardUserDao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PostgresCardUserDao implements CardUserDao {

    @Override
    public CardUser create(CardUser cardUser) {
        String sql = "INSERT INTO card_users (name, surname, iin) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cardUser.getName());
            pstmt.setString(2, cardUser.getSurname());
            pstmt.setString(3, cardUser.getIin());
            pstmt.executeUpdate();
            
            return cardUser;
        } catch (SQLException e) {
            System.err.println("Error creating card user: " + e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<CardUser> findByIin(String iin) {
        String sql = "SELECT * FROM card_users WHERE iin = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, iin);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(new CardUser(
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("iin")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding card user: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public Optional<CardUser> findByNameAndSurname(String name, String surname) {
        String sql = "SELECT * FROM card_users WHERE name = ? AND surname = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, name);
            pstmt.setString(2, surname);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return Optional.of(new CardUser(
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("iin")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding card user: " + e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public List<CardUser> findAll() {
        List<CardUser> users = new ArrayList<>();
        String sql = "SELECT * FROM card_users";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                users.add(new CardUser(
                    rs.getString("name"),
                    rs.getString("surname"),
                    rs.getString("iin")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error finding all card users: " + e.getMessage());
        }
        return users;
    }

    @Override
    public boolean update(CardUser cardUser) {
        String sql = "UPDATE card_users SET name = ?, surname = ? WHERE iin = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, cardUser.getName());
            pstmt.setString(2, cardUser.getSurname());
            pstmt.setString(3, cardUser.getIin());
            
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating card user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteByIin(String iin) {
        String sql = "DELETE FROM card_users WHERE iin = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, iin);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting card user: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean existsByIin(String iin) {
        String sql = "SELECT 1 FROM card_users WHERE iin = ?";
        try (Connection conn = DatabaseConnection.getPostgresConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, iin);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking card user existence: " + e.getMessage());
            return false;
        }
    }
}

