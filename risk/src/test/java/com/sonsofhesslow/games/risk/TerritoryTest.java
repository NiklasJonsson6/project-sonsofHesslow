package com.sonsofhesslow.games.risk;

import org.junit.Test;

import static org.junit.Assert.*;

public class TerritoryTest {
    @Test
    public void testNeighbour() throws Exception {
        Territory territory1 = new Territory(12);
        Territory territory2 = new Territory(13);
        Territory[] territory1Neighbours = new Territory[1];
        territory1Neighbours[0] = territory2;
        territory1.setNeighbours(territory1Neighbours);

        assertTrue(territory1.isNeighbour(territory2));
    }

}
