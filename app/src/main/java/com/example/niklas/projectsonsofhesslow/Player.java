package com.example.niklas.projectsonsofhesslow;

public class Player {
    private String name;
    private boolean isAlive;
    private int troopsToPlace;
    private boolean allowedToMove;
    //private Card[] cards;
    private Territory[] territoriesOwned;

    public Player(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    //to implement
    /*public Card[] getCards() {
        return cards;
    }

    public void setCards(Card[] cards) {
        this.cards = cards;
    }*/

    public Territory[] getTerritoriesOwned() {
        return territoriesOwned;
    }

    public void setTerritoriesOwned(Territory[] territoriesOwned) {
        this.territoriesOwned = territoriesOwned;
    }

    public void giveTroops(){
        int amountToGet = 5;

        int extraTroops = Territory.getExtraTroopAmount(territoriesOwned);
        amountToGet += extraTroops;

        troopsToPlace = amountToGet;
    }
}
