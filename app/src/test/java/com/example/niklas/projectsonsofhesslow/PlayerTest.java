package com.example.niklas.projectsonsofhesslow;

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
}
