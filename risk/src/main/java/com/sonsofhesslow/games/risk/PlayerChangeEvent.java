package com.sonsofhesslow.games.risk;

public class PlayerChangeEvent {
    Player oldPlayer;
    public Player newPlayer;

    public PlayerChangeEvent(Player oldPlayer, Player newPlayer) {
        this.oldPlayer = oldPlayer;
        this.newPlayer = newPlayer;
    }
}
