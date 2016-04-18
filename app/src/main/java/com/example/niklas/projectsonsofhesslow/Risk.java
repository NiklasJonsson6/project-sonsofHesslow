package com.example.niklas.projectsonsofhesslow;

import android.support.annotation.Nullable;
import android.view.View;

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
    private Territory defendingTerritory;
    private int territoriesPicked = 0;

    public Risk (int playerCount) {
        players = new Player[playerCount];
        for(int i = 0; i < playerCount; i++) {
            players[i] = new Player();
        }
        currentPlayer = players[0];
    }

    public void Handle(GL_TouchEvent event) {
        if(event.touchedRegion) {
            switch(gamePhase) {
                case PICK_TERRITORIES:
                    if(getTerritoryById(event.regionId) == null) {
                        territories[territoriesPicked] = new Territory(1, currentPlayer, null, event.regionId);
                        nextPlayer();
                        territoriesPicked++;

                        if(territoriesPicked == 42) gamePhase = GamePhase.PLACE_ARMIES;
                    }
                    break;

                case PLACE_ARMIES:
                    getTerritoryById(event.regionId).changeArmyCount(1);
                    if(currentPlayer.getTroopsToPlace() == 0) gamePhase = GamePhase.CHOOSE_ATTACKER;
                    break;

                case CHOOSE_ATTACKER:
                    //also check if any neighboring territories are hostile
                    if(getTerritoryById(event.regionId).getOccupier() == currentPlayer) {
                        GraphicsManager.setColor(event.regionId, attackerColor);
                        attackingTerritory = getTerritoryById(event.regionId);

                        gamePhase = GamePhase.CHOOSE_DEFENDER;
                    }
                    break;

                case CHOOSE_DEFENDER:
                    if(getTerritoryById(event.regionId).getOccupier() != currentPlayer) {
                        for(int i = 0; i < GraphicsManager.getNeighbours(attackingTerritory.getId()).length; i++) {
                            //does neighbors work this way? since the map is always the same, could it be much easier?
                            if(event.regionId == GraphicsManager.getNeighbours(attackingTerritory.getId())[i]) {
                                defendingTerritory = getTerritoryById(event.regionId);
                            }
                        }
                        //now show the fight button
                        break;
                    }
            }
        }
    }

    public void fightButtonPressed(View v) {
        Die.fight(attackingTerritory, defendingTerritory);
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
