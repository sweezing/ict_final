package org.example.dao;

import org.example.model.CardUser;

import java.util.List;
import java.util.Optional;

public interface CardUserDao {
    CardUser create(CardUser cardUser);
    Optional<CardUser> findByIin(String iin);
    Optional<CardUser> findByNameAndSurname(String name, String surname);
    List<CardUser> findAll();
    boolean update(CardUser cardUser);
    boolean deleteByIin(String iin);
    boolean existsByIin(String iin);
}

