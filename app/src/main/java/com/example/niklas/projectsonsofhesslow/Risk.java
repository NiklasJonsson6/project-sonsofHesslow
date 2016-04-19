package com.example.niklas.projectsonsofhesslow;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

import Graphics.GL_TouchEvent;
import Graphics.GL_TouchListener;
import Graphics.GraphicsManager;

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

        for(int i = 0; i < 42; i++) {
            territories[i] = new Territory(null);
        }

        for(int i = 0; i < 42; i++) {
            Integer[] ids = GraphicsManager.getNeighbours(territories[i].getId());
            int number = ids.length;
            Territory[] neighbours = new Territory[number];
            for(int k = 0; k < number; k++) {
                neighbours[k] = getTerritoryById(ids[k]);
            }
            territories[i].setNeighbours(neighbours);
        }
    }

    public void Handle(GL_TouchEvent event) {
        if(event.touchedRegion) {
            switch(gamePhase) {
                case PICK_TERRITORIES:
                    if(getTerritoryById(event.regionId) == null) {
                        territories[territoriesPicked].setOccupier(currentPlayer);
                        territories[territoriesPicked].setId(event.regionId);

                        nextPlayer();
                        territoriesPicked++;

                        if(territoriesPicked == 42) gamePhase = GamePhase.PLACE_ARMIES;
                    }
                    break;

                case PLACE_ARMIES:
                    if(getTerritoryById(event.regionId).getOccupier() == currentPlayer) {
                        getTerritoryById(event.regionId).changeArmyCount(1);
                        currentPlayer.decTroopsToPlace();
                    }
                    if(currentPlayer.getTroopsToPlace() == 0) {
                        gamePhase = GamePhase.CHOOSE_ATTACKER;
                    }
                    break;

                case CHOOSE_ATTACKER:
                    if(getTerritoryById(event.regionId).getOccupier() == currentPlayer) {
                        //checks if any neighboring territory can be attacked
                        for(int i = 0; i < getTerritoryById(event.regionId).getNeighbours().length; i++) {
                            if(getTerritoryById(event.regionId).getNeighbours()[i].getOccupier() != currentPlayer) {
                                attackingTerritory = getTerritoryById(event.regionId);

                                gamePhase = GamePhase.CHOOSE_DEFENDER;
                            }
                        }
                    }
                    break;

                case CHOOSE_DEFENDER:
                    if(getTerritoryById(event.regionId).getOccupier() != currentPlayer) {
                        for(int i = 0; i < attackingTerritory.getNeighbours().length; i++) {
                            if(getTerritoryById(event.regionId) == attackingTerritory.getNeighbours()[i]) {
                                defendingTerritory = getTerritoryById(event.regionId);
                                //TODO now show attack button
                            }
                        }
                    }
                    break;
            }
        }
    }

    public void fightButtonPressed(View v) {
        Die.fight(attackingTerritory, defendingTerritory);
    }

    public void endTurnPressed(View v) {
        nextPlayer();
        gamePhase = GamePhase.PLACE_ARMIES;
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
