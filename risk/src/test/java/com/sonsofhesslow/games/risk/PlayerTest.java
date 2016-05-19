package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Player;

import org.junit.Test;

import static org.junit.Assert.*;

public class PlayerTest {
    @Test
    public void testPlayerCreation() throws Exception {
        Player player = new Player();
        assertNotNull(player);
    }

    @Test
    public void testGetCard() throws Exception {
        Player player = new Player();
        player.giveOneCard();
        assertNotNull(player.getCards());
        assertEquals(1, player.getCards().size());
    }

    @Test
    public void giveTroops() throws Exception {
        Player player = new Player();
        player.giveArmies(4);
        player.decArmiesToPlace();
        assertEquals(3, player.getArmiesToPlace());
    }
}
