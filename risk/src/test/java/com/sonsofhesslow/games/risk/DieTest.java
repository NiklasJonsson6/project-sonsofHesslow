package com.sonsofhesslow.games.risk;

import org.junit.Test;

import static org.junit.Assert.*;

public class DieTest {
    @Test
    public void fight() throws Exception {
        Territory attackingTerritory = new Territory(0);
        Territory defendingTerritory = new Territory(1);
        attackingTerritory.setArmyCount(3);
        defendingTerritory.setArmyCount(2);
        attackingTerritory.setOccupier(new Player());
        defendingTerritory.setOccupier(new Player());
        int oldDefending = 0;
        int oldAttacking = 0;
        int numberKilledTroops = 0;

        while (attackingTerritory.getArmyCount() > 0 && defendingTerritory.getArmyCount() > 0) {
            oldAttacking = attackingTerritory.getArmyCount();
            oldDefending = defendingTerritory.getArmyCount();
            Die.fight(attackingTerritory, defendingTerritory);
            numberKilledTroops = (oldAttacking - attackingTerritory.getArmyCount()) + (oldDefending - defendingTerritory.getArmyCount());

            assertEquals(5-numberKilledTroops, attackingTerritory.getArmyCount()+defendingTerritory.getArmyCount());
        }
        /*if (numberWins >= 2) {
            assertEquals(true, attackingTerritory.getOccupier() == defendingTerritory.getOccupier());
            assertEquals(true, attackingTerritory.getArmyCount() > 0);
            assertEquals(false, defendingTerritory.getArmyCount() > 0);
        } else {
            assertEquals(false, attackingTerritory.getOccupier() == defendingTerritory.getOccupier());
            assertEquals(true, attackingTerritory.getArmyCount() == 1);
            assertEquals(true, defendingTerritory.getArmyCount() > 0);
        }*/
    }


}
