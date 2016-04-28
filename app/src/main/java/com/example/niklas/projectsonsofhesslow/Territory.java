package com.example.niklas.projectsonsofhesslow;

public class Territory {
    private int armyCount = 0;
    private Player occupier;
    private Continent continent;
    private int id;
    private Territory[] neighbours;

    public Territory(int id) {
        this.id = id;
    }

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


    public void changeArmyCount(int change){
        armyCount += change;
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
        switch(continentId) {
            case 0:
                continent = Continent.EUROPE;
                break;

            case 1:
                continent = Continent.OCEANIA;
                break;

            case 2:
                continent = Continent.SOUTH_AMERICA;
                break;

            case 3:
                continent = Continent.AFRICA;
                break;

            case 4:
                continent = Continent.NORTH_AMERICA;
                break;

            case 5:
                continent = Continent.ASIA;
                break;
        }
    }

    public Continent getContinent() {
        return continent;
    }
}
