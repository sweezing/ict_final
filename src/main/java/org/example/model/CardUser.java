package org.example.model;

public class CardUser {
    private String name;
    private String surname;
    private String iin;

    public CardUser() {
    }

    public CardUser(String name, String surname, String iin) {
        this.name = name;
        this.surname = surname;
        this.iin = iin;
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

    public String getIin() {
        return iin;
    }

    public void setIin(String iin) {
        this.iin = iin;
    }

    public String getFullName() {
        return name + " " + surname;
    }

    @Override
    public String toString() {
        return "CardUser{" +
                "name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", iin='" + iin + '\'' +
                '}';
    }
}
