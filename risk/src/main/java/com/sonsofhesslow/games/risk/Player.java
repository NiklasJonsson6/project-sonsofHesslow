package com.sonsofhesslow.games.risk;
import java.util.ArrayList;
import java.util.Random;

public class Player {
    private String name;
    private boolean isAlive;
    private int armiesToPlace;
    private boolean allowedToMove;
    ArrayList<Card> cards = new ArrayList<>();
    private int territoriesOwned;

    public Player() {
        Random rand = new Random();
        String[] name = {"Bilbo Baggins","Filibert Bolger","Fredegar Bolger","Mrs. Bracegirdle","Melilot Brandybuck","Rosie Cotton","Elanor Gamgee","Frodo Gamgee","Hamfast Gamgee","Farmer Maggot","Old Noakes","Mrs. Proudfoot","Odo Proudfoot","Otho Sackville-Baggins","Lobelia Sackville-Baggins","Ted Sandyman", "Diamond Took"};
        int n = rand.nextInt(name.length);
        this.name = name[n]; //TODO get name input somehow
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

    public void giveOneCard(){
        cards.add(Card.getRandomCard());
    }

    public int getTerritoriesOwned() {
        return territoriesOwned;
    }

    public void changeTerritoriesOwned(int change) {
        territoriesOwned += change;
    }

    public void setTerritoriesOwned(int amount){
        territoriesOwned = amount;
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
    public void decArmiesToPlace(int amount) {
        armiesToPlace-=amount;
    }
}