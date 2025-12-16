package org.example.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Card {
    private Integer cardId;
    private String pan;
    private String cvv;
    private String dateOfExpire;
    private String name;
    private String surname;
    private String currency;
    private Double balance;

    public Card() {
    }

    public Card(Integer cardId, String pan, String cvv, String dateOfExpire,
                String name, String surname, String currency, Double balance) {
        this.cardId = cardId;
        this.pan = pan;
        this.cvv = cvv;
        this.dateOfExpire = dateOfExpire;
        this.name = name;
        this.surname = surname;
        this.currency = currency;
        this.balance = balance;
    }

    public static String generateExpireDate() {
        LocalDate now = LocalDate.now();
        LocalDate expireDate = now.plusYears(1);
        return expireDate.format(DateTimeFormatter.ofPattern("yy/MM"));
    }

    public static String generateCVV() {
        int cvv = (int) (Math.random() * 900) + 100;
        return String.valueOf(cvv);
    }

    public Integer getCardId() {
        return cardId;
    }

    public void setCardId(Integer cardId) {
        this.cardId = cardId;
    }

    public String getPan() {
        return pan;
    }

    public void setPan(String pan) {
        this.pan = pan;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getDateOfExpire() {
        return dateOfExpire;
    }

    public void setDateOfExpire(String dateOfExpire) {
        this.dateOfExpire = dateOfExpire;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Double getBalance() {
        return balance;
    }

    public void setBalance(Double balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Card{" +
                "cardId=" + cardId +
                ", pan='" + pan + '\'' +
                ", cvv='" + cvv + '\'' +
                ", dateOfExpire='" + dateOfExpire + '\'' +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", currency='" + currency + '\'' +
                ", balance=" + balance +
                '}';
    }
}
