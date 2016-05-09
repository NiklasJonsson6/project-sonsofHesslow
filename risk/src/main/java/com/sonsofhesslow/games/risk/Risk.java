package com.sonsofhesslow.games.risk;

import java.util.ArrayList;
import java.util.List;

public class Risk {

    private Player[] players;
    private Player currentPlayer;
    private static GamePhase gamePhase;
    private Territory[] territories;
    private Territory attackingTerritory;
    private Territory defendingTerritory;
    private Territory selectedTerritory;
    private Territory secondSelectedTerritory;
    private ArrayList<Territory> defenders = new ArrayList<>(); //for view to show, maybe yellow outline?
    private ArrayList<Territory> neighbors = new ArrayList<>();

    enum GamePhase {PICK_TERRITORIES, PLACE_STARTING_ARMIES, PLACE_ARMIES, FIGHT, MOVEMENT}

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
        gamePhase = GamePhase.PICK_TERRITORIES;
    }

    void addAttackListener(RiskEventListener riskEventListener)
    {
        attackListeners.add(riskEventListener);
    }

    void addDefenceListeners(RiskEventListener riskEventListener)
    {
        deffenceListeners.add(riskEventListener);
    }

    void addSelectedListeners(RiskEventListener riskEventListener)
    {
        selectedListeners.add(riskEventListener);
    }
    void addSecondSelectedListeners(RiskEventListener riskEventListener)
    {
        secondSelectedListeners.add(riskEventListener);
    }

    void addOverlayListener(OverlayChangeListener overlayChangeListener){
        this.overlayChangeListener = overlayChangeListener;
    }
    OverlayChangeListener overlayChangeListener;
    List<RiskEventListener> attackListeners = new ArrayList<>();
    List<RiskEventListener> deffenceListeners = new ArrayList<>();
    List<RiskEventListener> selectedListeners = new ArrayList<>();
    List<RiskEventListener> secondSelectedListeners = new ArrayList<>();


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
        overlayChangeListener.playerChangeEvent(new OverlayChangeEvent(this));
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

    public ArrayList getNeighbors() {
        return neighbors;
    }
    public ArrayList getDefenders() {
        return defenders;
    }
    public Territory getSelectedTerritory(){return selectedTerritory;}
    public void setSelectedTerritory(Territory touchedTerritory){
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(this,touchedTerritory,this.selectedTerritory);
        for(RiskEventListener rl : selectedListeners) rl.changeEvent(riskChangeEvent);
        selectedTerritory = touchedTerritory;

    }
    public void setSecondSelectedTerritory(Territory touchedTerritory){
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(this,touchedTerritory,this.secondSelectedTerritory);
        for(RiskEventListener rl : secondSelectedListeners) rl.changeEvent(riskChangeEvent);
        secondSelectedTerritory = touchedTerritory;

    }
    public Territory getSecondSelectedTerritory(){return secondSelectedTerritory;}

    public GamePhase getGamePhase(){
        return gamePhase;
    }
    public void setGamePhase(GamePhase phase){
        System.out.println("Hej");
        overlayChangeListener.phaseEvent(new OverlayChangeEvent(this));
        this.gamePhase = phase;
    }
    public void placeEvent(){
        overlayChangeListener.placeEvent(new OverlayChangeEvent(this));
    }
}
