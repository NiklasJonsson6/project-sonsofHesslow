package com.example.niklas.projectsonsofhesslow;

public class Player {
    private String name;
    private boolean isAlive;
    private int troopsToPlace;
    private boolean allowedToMove;
    //private Card[] cards;
    //private Territory[] territoriesOwned;

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
    }/*

    /*public Territory[] getTerritoriesOwned() {
        return territoriesOwned;
    }

    public void setTerritoriesOwned(Territory[] territoriesOwned) {
        this.territoriesOwned = territoriesOwned;
    }*/

    public void giveTroops(){
        int amountToGet = 5;

        //to implement
        /*if(Territory.isOwningAsia(territoriesOwned)){
            amountToGet += 7;
        }
        if(Territory.isOwningNorthAmerica(territoriesOwned)){
            amountToGet += 5;
        }
        if(Territory.isOwningEurope(territoriesOwned)){
            amountToGet += 5;
        }
        if(Territory.isOwningAfrica(territoriesOwned)){
            amountToGet += 3;
        }
        if(Territory.isOwningOceania(territoriesOwned)){
            amountToGet += 2;
        }
        if(Territory.isOwningSouthAmerica(territoriesOwned)){
            amountToGet += 2;
        }*/

        troopsToPlace = amountToGet;
    }
}
