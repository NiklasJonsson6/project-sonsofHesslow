package com.sonsofhesslow.games.risk;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

public class Territory extends Observable {
    private int armyCount = 0;
    private Player occupier;
    private Continent continent;
    private int id;
    private Territory[] neighbours;

    public Territory(int id) {
        this.id = id;
        setArmyCount(1);
    }


    public int getArmyCount() {
        return armyCount;
    }

    public void setArmyCount(int armyCount) {
        ArmyChangeEvent event = new ArmyChangeEvent(this, this.armyCount, armyCount);
        notifyObservers(event);
        this.armyCount = armyCount;

        //TODO remove old listeners
        for (ArmyChangeListener listener : armyListeners) listener.handle(event);
    }

    public Player getOccupier() {
        return occupier;
    }

    public void setOccupier(Player occupier) {
        if (occupier != this.occupier) {
            OccupierChangeEvent event = new OccupierChangeEvent(this, this.occupier, occupier);
            notifyObservers(event);

            //TODO remove old listeners
            for (OccupierChangeListener listener : occupierListeners) listener.handle(event);
        }
        if (this.occupier != null) {
            this.occupier.changeTerritoriesOwned(-1);
        }
        occupier.changeTerritoriesOwned(1);
        this.occupier = occupier;
    }

    public void changeArmyCount(int change) {
        setArmyCount(armyCount + change);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Territory[] getNeighbours() {
        return neighbours;
    }

    public boolean isNeighbour(Territory territory) {
        for (Territory neighbour : this.getNeighbours()) {
            if (neighbour == territory) return true;
        }
        return false;
    }

    public void setNeighbours(Territory[] neighbours) {
        this.neighbours = neighbours;
    }

    public void setContinent(int continentId) {
        continent = Continent.values()[continentId];
    }

    public Continent getContinent() {
        return continent;
    }

    //listeners  boilerplate
    /*
    Armies
     */
    public static class ArmyChangeEvent {
        Territory territory;
        int oldValue;
        //public for NetworkManager...
        public int newValue;

        public ArmyChangeEvent(Territory territory, int oldValue, int newValue) {
            this.territory = territory;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    //TODO remove these
    public interface ArmyChangeListener {
        void handle(ArmyChangeEvent armyChangeEvent);
    }
    List<ArmyChangeListener> armyListeners = new ArrayList<>();
    public void addArmyListeners(ArmyChangeListener listener) {
        armyListeners.add(listener);
    }

    /*
    Occupier
     */
    public static class OccupierChangeEvent {
        Territory territory;
        Player oldValue;
        //public for NetworkManager...
        public Player newValue;

        public OccupierChangeEvent(Territory territory, Player oldValue, Player newValue) {
            this.territory = territory;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    //TODO remove these
    public interface OccupierChangeListener {
        void handle(OccupierChangeEvent occupierChangeEvent);
    }
    List<OccupierChangeListener> occupierListeners = new ArrayList<>();
    public void addOccupierListeners(OccupierChangeListener listener) {
        occupierListeners.add(listener);
    }
}
