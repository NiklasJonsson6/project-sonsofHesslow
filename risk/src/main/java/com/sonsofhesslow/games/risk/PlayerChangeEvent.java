package com.sonsofhesslow.games.risk;

import com.sonsofhesslow.games.risk.model.Player;

public class PlayerChangeEvent {
    Player oldPlayer;
    public Player newPlayer;

    public PlayerChangeEvent(Player oldPlayer, Player newPlayer) {
        this.oldPlayer = oldPlayer;
        this.newPlayer = newPlayer;
    }
}
