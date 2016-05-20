package com.sonsofhesslow.games.risk.model;

import com.sonsofhesslow.games.risk.OverlayChangeEvent;
import com.sonsofhesslow.games.risk.OverlayChangeListener;
import com.sonsofhesslow.games.risk.PlayerChangeEvent;
import com.sonsofhesslow.games.risk.PlayerChangeEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Risk extends Observable {

    private Player[] players;
    private Player currentPlayer;
    private static GamePhase gamePhase;
    private Territory[] territories;
    private Territory attackingTerritory;
    private Territory defendingTerritory;
    private Territory selectedTerritory;
    private Territory secondSelectedTerritory;
    private ArrayList<Territory> defenders = new ArrayList<>();
    private ArrayList<Territory> neighbors = new ArrayList<>();

    public enum GamePhase {PICK_TERRITORIES, PLACE_STARTING_ARMIES, PLACE_ARMIES, FIGHT, MOVEMENT}

    public Risk(int playerIds[], int territoryCount) {
        territories = new Territory[territoryCount];

        players = new Player[playerIds.length];
        for (int i = 0; i < playerIds.length; i++) {
            players[i] = new Player(playerIds[i]);
        }
        //create territory objects
        for (int i = 0; i < territoryCount; i++) {
            territories[i] = new Territory(i);
        }
        gamePhase = GamePhase.PICK_TERRITORIES;
    }

    public void setAttackingTerritory(Territory territory) {
        System.out.println("attacking contry set");
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.ATTACK, this, territory, this.attackingTerritory);

        setChanged();
        notifyObservers(riskChangeEvent);

        //TODO remove old listeners
        //for (RiskEventListener rl : attackListeners) rl.changeEvent(riskChangeEvent);

        attackingTerritory = territory;
    }

    public Territory getAttackingTerritory() {
        return attackingTerritory;
    }

    public void setDefendingTerritory(Territory territory) {
        System.out.println("defending contry set");
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.DEFENCE, this, territory, this.defendingTerritory);

        setChanged();
        notifyObservers(riskChangeEvent);

        //TODO remove old listeners
        //for (RiskEventListener rl : defenceListeners) rl.changeEvent(riskChangeEvent);

        defendingTerritory = territory;
    }

    public Territory getDefendingTerritory() {
        return defendingTerritory;
    }

    public void setCurrentPlayer(Player player) {
        System.out.println("current player: " + getCurrentPlayer() + " new player: " + player);
        currentPlayer = player;
        /* for(PlayerChangeEventListener playerChangeListener : playerChangeListeners){
            System.out.println("calling changeevent");
            playerChangeListener.changeEvent(new PlayerChangeEvent(getCurrentPlayer() ,player));
        } */
        setChanged();
        notifyObservers(player);
        //overlayChangeListener.playerChangeEvent(new OverlayChangeEvent(this));
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

    public Territory getSelectedTerritory() {
        return selectedTerritory;
    }

    public void setSelectedTerritory(Territory touchedTerritory) {
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.SELECTED, this, touchedTerritory, this.selectedTerritory);

        setChanged();
        notifyObservers(riskChangeEvent);

        //TODO remove old listeners
        //for (RiskEventListener rl : selectedListeners) rl.changeEvent(riskChangeEvent);

        selectedTerritory = touchedTerritory;

    }

    public void setSecondSelectedTerritory(Territory touchedTerritory) {
        RiskChangeEvent riskChangeEvent = new RiskChangeEvent(RiskChangeEvent.EventType.SECOND_SELECTED, this, touchedTerritory, this.secondSelectedTerritory);

        setChanged();
        notifyObservers(riskChangeEvent);

        //TODO remove old listeners
        //for (RiskEventListener rl : secondSelectedListeners) rl.changeEvent(riskChangeEvent);

        secondSelectedTerritory = touchedTerritory;
    }

    public Territory getSecondSelectedTerritory() {
        return secondSelectedTerritory;
    }

    public GamePhase getGamePhase() {
        return gamePhase;
    }

    public void setGamePhase(GamePhase phase) {
        this.gamePhase = phase;

        setChanged();
        notifyObservers(phase);

        //overlayChangeListener.phaseEvent(new OverlayChangeEvent(this));
    }

    public void placeEvent() {
        setChanged();
        notifyObservers();

        //overlayChangeListener.placeEvent(new OverlayChangeEvent(this));
    }

    /*
    Listeners
     */
    public static class RiskChangeEvent {
        public enum EventType {ATTACK, DEFENCE, SELECTED, SECOND_SELECTED}

        public EventType eventType;
        Risk risk;
        public Territory newTerritory;
        public Territory oldTerritory;

        public RiskChangeEvent(EventType eventType, Risk risk, Territory newTerritory, Territory oldTerritory) {
            this.eventType = eventType;
            this.risk = risk;
            this.newTerritory = newTerritory;
            this.oldTerritory = oldTerritory;
        }
    }

    //TODO remove this shit
    /* void addAttackListener(RiskEventListener riskEventListener) {
        attackListeners.add(riskEventListener);
    }

    void addDefenceListeners(RiskEventListener riskEventListener) {
        defenceListeners.add(riskEventListener);
    }

    void addSelectedListeners(RiskEventListener riskEventListener) {
        selectedListeners.add(riskEventListener);
    }

    void addSecondSelectedListeners(RiskEventListener riskEventListener) {
        secondSelectedListeners.add(riskEventListener);
    }

    public void addOverlayListener(OverlayChangeListener overlayChangeListener) {
        this.overlayChangeListener = overlayChangeListener;
    }

    OverlayChangeListener overlayChangeListener;
    List<RiskEventListener> attackListeners = new ArrayList<>();
    List<RiskEventListener> defenceListeners = new ArrayList<>();
    List<RiskEventListener> selectedListeners = new ArrayList<>();
    List<RiskEventListener> secondSelectedListeners = new ArrayList<>();
    List<PlayerChangeEventListener> playerChangeListeners = new ArrayList<>();

    public void addPlayerChangeListener(PlayerChangeEventListener playerChangeListener){
        playerChangeListeners.add(playerChangeListener);
    }

    public void removePlayerChangeListener(PlayerChangeEventListener playerChangeListener){
        playerChangeListeners.remove(playerChangeListener);
    }

    interface RiskEventListener {
        void changeEvent(RiskChangeEvent riskChangeEvent);
    } */

}
