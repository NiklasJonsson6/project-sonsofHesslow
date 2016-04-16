package com.example.niklas.projectsonsofhesslow;
import java.util.ArrayList;

public class Player {
    private String name;
    private boolean isAlive;
    private int troopsToPlace;
    private boolean allowedToMove;
    ArrayList<Card> cards;
    private Territory[] territoriesOwned;

    public Player() {
        this.name = ""; //TODO get name input somehow
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

    public ArrayList<Card> getCards() {
        return cards;
    }

    public void setCards(ArrayList<Card> cards) {
        this.cards = cards;
    }

    public Territory[] getTerritoriesOwned() {
        return territoriesOwned;
    }

    public void setTerritoriesOwned(Territory[] territoriesOwned) {
        this.territoriesOwned = territoriesOwned;
    }

    public void giveTroops(){
        int amountToGet = 5;

        //user has to hand in cards if you have 5 or more cards
        if(cards.size() > 4){
            if(Card.canHandInSet(cards)){
                Card.handInSet(cards);
            }
            amountToGet += Card.cardAmountToGet();
        }
        amountToGet = Territory.getExtraTroopAmount(territoriesOwned);

        troopsToPlace = amountToGet;
    }
}
