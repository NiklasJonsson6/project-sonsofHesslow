package com.sonsofhesslow.games.risk;

import java.util.ArrayList;
import java.util.List;

public class Territory {
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
        this.armyCount = armyCount;
        for (ArmyChangeListener listener : armyListeners) listener.handle(event);
    }

    public Player getOccupier() {
        return occupier;
    }

    public void setOccupier(Player occupier) {
        if (occupier != this.occupier) {
            OccupierChangeEvent event = new OccupierChangeEvent(this, this.occupier, occupier);
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
    public class ArmyChangeEvent {
        Territory territory;
        int oldValue;
        public int newValue;

        public ArmyChangeEvent(Territory territory, int oldValue, int newValue) {
            this.territory = territory;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

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
    public class OccupierChangeEvent {
        Territory territory;
        Player oldValue;
        public Player newValue;

        public OccupierChangeEvent(Territory territory, Player oldValue, Player newValue) {
            this.territory = territory;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }

    public interface OccupierChangeListener {
        void handle(OccupierChangeEvent occupierChangeEvent);
    }

    List<OccupierChangeListener> occupierListeners = new ArrayList<>();

    public void addOwnerListeners(OccupierChangeListener listener) {
        occupierListeners.add(listener);
    }
}
