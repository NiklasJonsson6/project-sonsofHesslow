package com.example.niklas.projectsonsofhesslow;

import android.graphics.Color;

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

    public Risk (int playerCount) {
        players = new Player[playerCount];
        for(int i = 0; i < playerCount; i++) {
            players[i] = new Player();
        }
    }

    public void Handle(GL_TouchEvent event) {
        //place armies
        if (event.touchedRegion && gamePhase == GamePhase.PLACE_ARMIES) {
            
        }

        //attack
        if (event.touchedRegion && gamePhase == GamePhase.PICK_TERRITORIES) {
            GraphicsManager.setColor(event.regionId, attackerColor);
            //attacker =
        }
    }
}
