package com.example.niklas.projectsonsofhesslow;
import java.util.ArrayList;

public class Player {
    private String name;
    private boolean isAlive;
    private int armiesToPlace;
    private boolean allowedToMove;
    ArrayList<Card> cards;
    private int territoriesOwned;

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

    public int getTerritoriesOwned() {
        return territoriesOwned;
    }

    public void setTerritoriesOwned(int change) {
        territoriesOwned += change;
    }

    public void giveArmies(int change) {
        armiesToPlace += change;
    }

    public int getArmiesToPlace() {
        return armiesToPlace;
    }

    public void decArmiesToPlace() {
        armiesToPlace--;
    }
}
