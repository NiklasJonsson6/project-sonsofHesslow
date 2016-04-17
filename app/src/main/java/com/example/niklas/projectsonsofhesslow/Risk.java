package com.example.niklas.projectsonsofhesslow;

import android.support.annotation.Nullable;

import Graphics.GL_TouchEvent;
import Graphics.GL_TouchListener;
import Graphics.GraphicsManager;

/**
 * Created by Niklas on 14/04/16.
 */
public class Risk implements GL_TouchListener {
    private enum GamePhase {PICK_TERRITORIES, PLACE_ARMIES, CHOOSE_ATTACKER, CHOOSE_DEFENDER}
    private GamePhase gamePhase = GamePhase.PICK_TERRITORIES;

    private float[] attackerColor = {1, 0, 0};
    private Player[] players;
    private Player currentPlayer;
    private int currentPlayerTracker = 0; //used to set next player
    private Territory[] territories = new Territory[42];
    private Territory attackingTerritory;
    private int territoriesPicked = 0;

    public Risk (int playerCount) {
        players = new Player[playerCount];
        for(int i = 0; i < playerCount; i++) {
            players[i] = new Player();
        }
    }

    public void Handle(GL_TouchEvent event) {
        //pick territories
        if (event.touchedRegion && gamePhase == GamePhase.PICK_TERRITORIES && getTerritoryById(event.regionId) == null) {
            territories[territoriesPicked] = new Territory(1, currentPlayer, null, event.regionId);
            nextPlayer();
            territoriesPicked++;

            if(territoriesPicked == 42) gamePhase = GamePhase.PLACE_ARMIES;
        }

        //place armies
        if (event.touchedRegion && gamePhase == GamePhase.PLACE_ARMIES) {
            getTerritoryById(event.regionId).changeArmyCount(1);
        }

        //attack
        if (event.touchedRegion && gamePhase == GamePhase.CHOOSE_ATTACKER && getTerritoryById(event.regionId).getOccupier() == currentPlayer) {
            GraphicsManager.setColor(event.regionId, attackerColor);
            attackingTerritory = getTerritoryById(event.regionId);
            //...
        }
    }

    @Nullable
    private Territory getTerritoryById(int id) {
        for(int i = 0; i < 42; i++) {
            if(territories[i].getId() == id) return territories[i];
        }
        return null;
    }

    private void nextPlayer() {
        currentPlayerTracker++;
        if(currentPlayerTracker == players.length) currentPlayerTracker = 0;
        currentPlayer = players[currentPlayerTracker];
    }
}
