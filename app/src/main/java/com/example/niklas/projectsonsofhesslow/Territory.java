package com.example.niklas.projectsonsofhesslow;

public class Territory {
    private int armyCount = 0;
    private Player occupier;
    public final Continent continent;
    private int id;
    private Territory[] neighbours;

    public int getArmyCount() {
        return armyCount;
    }

    public void setArmyCount(int armyCount) {
        this.armyCount = armyCount;
    }

    public Player getOccupier() {
        return occupier;
    }

    public void setOccupier(Player occupier) {
        this.occupier = occupier;
    }

    public Territory(Continent continent) {
        this.continent = continent;
    }

    public void changeArmyCount(int change){
        armyCount += change;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    //public void

    public Territory[] getNeighbours() {
        return neighbours;
    }

    public void setNeighbours(Territory[] neighbours) {
        this.neighbours = neighbours;
    }
}
