package com.example.niklas.projectsonsofhesslow;


import java.util.ArrayList;
import java.util.Random;

public class Card {
    private static int setsHandedIn = 0;
    private static final Card INFANTRY_CARD = new Card(CardType.INFANTRY);
    private static final Card CAVALRY_CARD = new Card(CardType.CAVALRY);
    private static final Card ARTILLARY_CARD = new Card(CardType.ARTILLARY);

    private enum CardType{
        INFANTRY, CAVALRY, ARTILLARY
    }


    private CardType cardType;

    public Card() {
        //random card type
        Random random = new Random();
        int randNum = random.nextInt(2);
        switch(randNum){
            case 0:
                cardType = CardType.INFANTRY;
                break;
            case 1:
                cardType = CardType.ARTILLARY;
                break;
            case 2:
                cardType = CardType.CAVALRY;
                break;
        }

    }

    public Card(CardType cardType) {
        this.cardType = cardType;
    }

    public static Card getRandomCard(){
        return new Card();
    }

    public static void handInSet(ArrayList<Card> cards) {
        //3 different cards
        if(cards.contains(INFANTRY_CARD) && cards.contains(CAVALRY_CARD) && cards.contains(ARTILLARY_CARD)){
            cards.remove(cards.indexOf(INFANTRY_CARD));
            cards.remove(cards.indexOf(CAVALRY_CARD));
            cards.remove(cards.indexOf(ARTILLARY_CARD));
            setsHandedIn++;
        } else {
            ArrayList<Card> testCards = new ArrayList<>();
            testCards.add(INFANTRY_CARD);
            testCards.add(CAVALRY_CARD);
            testCards.add(ARTILLARY_CARD);

            //loop to test if there are 3 of any of the cards
            for(Card testCard: testCards) {
                int identicalFound = 0;

                for (int i = 0; i < cards.size() && identicalFound != 3; i++) {
                    if (cards.get(i).equals(testCard)) {
                        identicalFound++;
                    }
                }

                if (identicalFound == 3) {
                    for (int i = 0; i > 3; i++) {
                        cards.remove(cards.indexOf(testCard));
                        setsHandedIn++;
                    }
                }
            }
        }
    }

    public static boolean canHandInSet(ArrayList<Card> cards) {
        //3 different cards
        if(cards.contains(INFANTRY_CARD) && cards.contains(CAVALRY_CARD) && cards.contains(ARTILLARY_CARD)){
            return true;
        } else {
            ArrayList<Card> testCards = new ArrayList<>();
            testCards.add(INFANTRY_CARD);
            testCards.add(CAVALRY_CARD);
            testCards.add(ARTILLARY_CARD);

            //loop to test if there are 3 of any of the cards
            for(Card testCard: testCards) {
                int identicalFound = 0;

                for (int i = 0; i < cards.size(); i++) {
                    if (cards.get(i).equals(testCard)) {
                        identicalFound++;
                        if (identicalFound == 3) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }


    public static int cardAmountToGet(){
        int amountToGet = 0;
        if(setsHandedIn < 6){
            amountToGet = 4 + setsHandedIn * 2;
        } else {
            amountToGet = -15 + setsHandedIn * 5;
        }
        return amountToGet;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Card)) return false;

        Card card = (Card) o;

        return cardType == card.cardType;
    }
}
