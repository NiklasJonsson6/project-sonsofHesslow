package com.example.niklas.projectsonsofhesslow;
public class Risk {

    private Player[] players;
    private Player currentPlayer;
    private Territory[] territories;
    private Territory attackingTerritory;
    private Territory defendingTerritory;

    public Risk (int playerCount, int territoryCount) {
        territories = new Territory[territoryCount];

        players = new Player[playerCount];
        for(int i = 0; i < playerCount; i++) {
            players[i] = new Player();
        }

        //create territory objects
        for(int i = 0; i < territoryCount; i++) {
            territories[i] = new Territory(null, i); //TODO continent
        }
    }

    //setters getters
    public void setAttackingTerritory(Territory territory) {
        attackingTerritory = territory;
    }
    public Territory getAttackingTerritory() {
        return attackingTerritory;
    }

    public void setDefendingTerritory(Territory territory) {
        defendingTerritory = territory;
    }
    public Territory getDefendingTerritory() {
        return defendingTerritory;
    }

    public void setCurrentPlayer (Player player) {
        currentPlayer = player;
    }
    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Territory[] getTerritories() {
        return territories;
    }

    public Player[] getPlayers() {
        return players;
    }
}
