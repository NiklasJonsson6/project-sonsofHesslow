package com.example.niklas.projectsonsofhesslow;

public class Territory {
    private int armyCount;
    private Player occupier;
    public final Continent continent;
    private int id;

    //TODO change when done to correct amounts
    private static final int TERRITORIES_IN_ASIA = 10;
    private static final int TERRITORIES_IN_NORTH_AMERICA = 10;
    private static final int TERRITORIES_IN_EUROPE = 10;
    private static final int TERRITORIES_IN_AFRICA = 10;
    private static final int TERRITORIES_IN_OCEANIA = 10;
    private static final int TERRITORIES_IN_SOUTH_AMERICA = 10;

    private static final int EXTRA_TROOPS_ASIA = 7;
    private static final int EXTRA_TROOPS_NORTH_AMERICA = 5;
    private static final int EXTRA_TROOPS_EUROPE = 5;
    private static final int EXTRA_TROOPS_AFRICA = 3;
    private static final int EXTRA_TROOPS_OCEANIA = 2;
    private static final int EXTRA_TROOPS_SOUTH_AMERICA = 2;

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

    public Territory(int armyCount, Player occupier, Continent continent, int id){
        this.armyCount = armyCount;
        this.occupier = occupier;
        this.continent = continent;
        this.id = id;
    }

    public void changeArmyCount(int change){
        armyCount += change;
    }

    public void assignTerritory(Player player){
        occupier = player;
    }

    //extra troops for owning continents
    public static int getExtraTroopAmount(Territory[] territories){
        int territoriesFoundAsia = 0;
        int territoriesFoundNorthAmerica = 0;
        int territoriesFoundEurope = 0;
        int territoriesFoundAfrica = 0;
        int territoriesFoundOceania = 0;
        int territoriesFoundSouthAmerica = 0;
        
        for(Territory territory: territories){
            switch (territory.continent){
                case ASIA:
                    territoriesFoundAsia++;
                    break;
                case NORTH_AMERICA:
                    territoriesFoundNorthAmerica++;
                    break;
                case EUROPE:
                    territoriesFoundEurope++;
                    break;
                case AFRICA:
                    territoriesFoundAfrica++;
                    break;
                case OCEANIA:
                    territoriesFoundOceania++;
                    break;
                case SOUTH_AMERICA:
                    territoriesFoundSouthAmerica++;
                    break;
            }
        }

        int extraTroops = 0;

        //if owning a whole continent, add corresponding extra troop amounts:
        if(territoriesFoundAsia == TERRITORIES_IN_ASIA){
            extraTroops += EXTRA_TROOPS_ASIA;
        }
        if(territoriesFoundNorthAmerica == TERRITORIES_IN_NORTH_AMERICA){
            extraTroops += EXTRA_TROOPS_NORTH_AMERICA;
        }
        if(territoriesFoundEurope == TERRITORIES_IN_EUROPE){
            extraTroops += EXTRA_TROOPS_EUROPE;
        }
        if(territoriesFoundAfrica == TERRITORIES_IN_AFRICA){
            extraTroops += EXTRA_TROOPS_AFRICA;
        }
        if(territoriesFoundOceania == TERRITORIES_IN_OCEANIA){
            extraTroops += EXTRA_TROOPS_OCEANIA;
        }
        if(territoriesFoundSouthAmerica == TERRITORIES_IN_SOUTH_AMERICA){
            extraTroops += EXTRA_TROOPS_SOUTH_AMERICA;
        }
        
        return extraTroops;
    }

    public void removeTroop(){
        this.armyCount--;
    }

    public int getId() {
        return id;
    }
}
