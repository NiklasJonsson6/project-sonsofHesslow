package com.example.niklas.projectsonsofhesslow;

public class Territory {
    private int armyCount;
    private Player occupier;
    public final Continent continent;

    public Territory(int armyCount, Player occupier, Continent continent){
        this.armyCount = armyCount;
        this.occupier = occupier;
        this.continent = continent;
    }

    public void changeArmyCount(int change){
        armyCount += change;
    }

    public void assignTerritory(Player player){
        occupier = player;
    }
}
