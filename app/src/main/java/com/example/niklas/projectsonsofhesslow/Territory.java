package com.example.niklas.projectsonsofhesslow;

import java.security.acl.Owner;
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
        TroupChangeEvent event = new TroupChangeEvent(this,this.armyCount,armyCount);
        this.armyCount = armyCount;
        for(TroupChangeListener listener : troupListeners) listener.handle(event);
    }

    public Player getOccupier() {
        return occupier;
    }

    public void setOccupier(Player occupier) {
        if(occupier != this.occupier)
        {
            OwnerChangeEvent event = new OwnerChangeEvent(this,this.occupier,occupier);
            for(OwnerChangeListener listener : ownerListeners) listener.handle(event);
        }
        if(this.occupier != null) {
            this.occupier.changeTerritoriesOwned(-1);
        }
        occupier.changeTerritoriesOwned(1);
        this.occupier = occupier;
    }

    public void changeArmyCount(int change){
        setArmyCount(armyCount+change);
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
        for(Territory neighbour: this.getNeighbours()) {
            if(neighbour == territory) return true;
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
    class TroupChangeEvent{Territory territory;int oldValue;int newValue;

        public TroupChangeEvent(Territory territory, int oldValue, int newValue) {
            this.territory = territory;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
    interface TroupChangeListener{void handle(TroupChangeEvent troupChangeEvent);}
    List<TroupChangeListener> troupListeners = new ArrayList<>();
    void addTroupListeners(TroupChangeListener listener){troupListeners.add(listener);}
    class OwnerChangeEvent{
        Territory territory;
        Player oldValue;
        Player newValue;

        public OwnerChangeEvent(Territory territory, Player oldValue, Player newValue) {
            this.territory = territory;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }
    }
    interface OwnerChangeListener{void handle(OwnerChangeEvent ownerChangeEvent);}
    List<OwnerChangeListener> ownerListeners = new ArrayList<>();
    void addOwnerListeners(OwnerChangeListener listener){ownerListeners.add(listener);}
}
