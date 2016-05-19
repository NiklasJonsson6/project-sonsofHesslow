package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Player;

import org.junit.Test;

import static org.junit.Assert.*;

public class CardTest {
    @Test
    public void testCardCreation() throws Exception {
        Card card = new Card();
        assertNotNull(card);
    }

    @Test
    public void giveTroops() throws Exception {
        Player player = new Player();
        player.giveArmies(4);
        player.decArmiesToPlace();
        assertEquals(3, player.getArmiesToPlace());
    }

    @Test
    public void testHandInSet() throws Exception {
        Player player = new Player();
        //as if player gained cards under a longer period
        for(int i = 0; i < 5; i++){
            player.giveOneCard();
        }
        assertEquals(5, player.getCards().size());
        assertTrue(Card.canHandInSet(player.getCards()));
        Card.handInSet(player.getCards());
        assertEquals(2, player.getCards().size());
    }
}
