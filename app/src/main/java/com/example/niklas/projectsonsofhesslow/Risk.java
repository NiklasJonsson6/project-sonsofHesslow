package com.example.niklas.projectsonsofhesslow;

import java.util.ArrayList;
import java.util.List;

public class Risk {

    private Player[] players;
    private Player currentPlayer;
    private Territory[] territories;
    private Territory attackingTerritory;
    private Territory defendingTerritory;
    private ArrayList<Territory> defenders = new ArrayList<>();

    public Risk (int playerCount, int territoryCount) {
        territories = new Territory[territoryCount];

        players = new Player[playerCount];
        for(int i = 0; i < playerCount; i++) {
            players[i] = new Player();
        }
        //create territory objects
        for(int i = 0; i < territoryCount; i++) {
            territories[i] = new Territory(i); //TODO continent
        }
        new View(this);
    }

    void addAttackListener(RiskEventListener riskEventListener)
    {
        attackListeners.add(riskEventListener);
    }

    void addDefenceListeners(RiskEventListener riskEventListener)
    {
        deffenceListeners.add(riskEventListener);
    }

    List<RiskEventListener> attackListeners = new ArrayList<>();
    List<RiskEventListener> deffenceListeners = new ArrayList<>();

    static class RiskChangeEvent
    {
        public RiskChangeEvent(Risk risk, Territory newTerritory, Territory oldTerritory) {
            this.risk = risk;
            this.newTerritory = newTerritory;
            this.oldTerritory = oldTerritory;
        }

        Risk risk;
        Territory newTerritory;
        Territory oldTerritory;
    }

    interface RiskEventListener
    {
        void changeEvent(RiskChangeEvent riskChangeEvent);
    }


    //setters getters
    public void setAttackingTerritory(Territory territory)
    {
        System.out.println("attacking contry set");
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(this,territory,this.attackingTerritory);
        for(RiskEventListener rl : attackListeners) rl.changeEvent(riskChangeEvent);
        attackingTerritory = territory;
    }
    public Territory getAttackingTerritory() {
        return attackingTerritory;
    }

    public void setDefendingTerritory(Territory territory)
    {
        System.out.println("deffending contry set");
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(this,territory,this.defendingTerritory);
        for(RiskEventListener rl : deffenceListeners) rl.changeEvent(riskChangeEvent);
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

    public ArrayList getDefenders() {
        return defenders;
    }
}
