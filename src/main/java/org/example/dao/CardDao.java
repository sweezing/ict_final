package org.example.dao;

import org.example.model.Card;

import java.util.List;
import java.util.Optional;

public interface CardDao {
    Card create(Card card);
    Optional<Card> findById(Integer cardId);
    Optional<Card> findByPan(String pan);
    List<Card> findByNameAndSurname(String name, String surname);
    List<Card> findAll();
    boolean update(Card card);
    boolean deleteById(Integer cardId);
    boolean deleteByPan(String pan);
    boolean existsByPan(String pan);
    boolean transferMoney(String fromPan, String toPan, Double amount);
    boolean transferMoneyByName(String fromName, String fromSurname, 
                                String toName, String toSurname, Double amount);
    boolean withdrawMoney(String pan, String cvv, Double amount);
    boolean depositMoney(String pan, Double amount);
    boolean depositMoneyByName(String name, String surname, Double amount);
}

