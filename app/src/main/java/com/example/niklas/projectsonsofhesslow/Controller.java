package com.example.niklas.projectsonsofhesslow;

import android.support.annotation.Nullable;
import android.view.View;

import Graphics.GL_TouchEvent;
import Graphics.GL_TouchListener;
import Graphics.GraphicsManager;

/**
 * Created by Niklas on 2016-04-21.
 */
public class Controller implements GL_TouchListener {
    private Risk riskModel;

    private enum GamePhase {PICK_TERRITORIES, PLACE_ARMIES, CHOOSE_ATTACKER, CHOOSE_DEFENDER}
    private GamePhase gamePhase = GamePhase.PICK_TERRITORIES;

    private int currentPlayerTracker = 0; //used to set next player
    private int territoriesPicked = 0;

    public Controller() {
        riskModel = new Risk(2); //somehow set number of players (2)
        //view observer thing?

        riskModel.setCurrentPlayer(riskModel.getPlayers()[0]);

        for(int i = 0; i < 42; i++) {
            Integer[] ids = GraphicsManager.getNeighbours(riskModel.getTerritories()[i].getId());
            int number = ids.length;
            Territory[] neighbours = new Territory[number];

            for(int k = 0; k < number; k++) {
                neighbours[k] = getTerritoryById(ids[k]);
            }

            riskModel.getTerritories()[i].setNeighbours(neighbours);
        }
    }

    public void Handle(GL_TouchEvent event) {
        if(event.touchedRegion) {
            Territory touchedTerritory = getTerritoryById(event.regionId);
            switch(gamePhase) {
                case PICK_TERRITORIES:
                    if(touchedTerritory == null && touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                        riskModel.getTerritories()[territoriesPicked].setOccupier(riskModel.getCurrentPlayer());

                        territoriesPicked++;
                        nextPlayer();
                        if(territoriesPicked == 42) {
                            gamePhase = GamePhase.PLACE_ARMIES;
                            setArmiesToPlace();
                        }
                    }
                    break;

                case PLACE_ARMIES:
                    if(touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                        touchedTerritory.changeArmyCount(1);
                        riskModel.getCurrentPlayer().decArmiesToPlace();
                    }
                    if(riskModel.getCurrentPlayer().getArmiesToPlace() == 0) {
                        gamePhase = GamePhase.CHOOSE_ATTACKER;
                    }
                    break;

                case CHOOSE_ATTACKER:
                    if(touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                        //checks if any neighboring territory can be attacked
                        for(int i = 0; i < touchedTerritory.getNeighbours().length; i++) {
                            if(touchedTerritory.getNeighbours()[i].getOccupier() != riskModel.getCurrentPlayer()) {
                                riskModel.setAttackingTerritory(touchedTerritory);

                                gamePhase = GamePhase.CHOOSE_DEFENDER;
                            }
                        }
                    }
                    break;

                case CHOOSE_DEFENDER:
                    if(touchedTerritory.getOccupier() != riskModel.getCurrentPlayer()) {
                        if(touchedTerritory.isNeighbour(riskModel.getAttackingTerritory())) {
                            riskModel.setDefendingTerritory(touchedTerritory);
                            //TODO now show attack button
                        }
                    }
                    break;
            }
        }
    }

    public void fightButtonPressed(View v) {
        Die.fight(riskModel.getAttackingTerritory(), riskModel.getDefendingTerritory());
    }

    public void endTurnPressed(View v) {
        nextPlayer();
        gamePhase = GamePhase.PLACE_ARMIES;
    }

    private void nextPlayer() {
        currentPlayerTracker++;
        if(currentPlayerTracker == riskModel.getPlayers().length) currentPlayerTracker = 0;
        riskModel.setCurrentPlayer(riskModel.getPlayers()[currentPlayerTracker]);

        if(gamePhase == GamePhase.PICK_TERRITORIES) {
            riskModel.getCurrentPlayer().giveArmies(1);
        } else {
            setArmiesToPlace();
        }
    }

    @Nullable
    private Territory getTerritoryById(int id) {
        for(Territory territory: riskModel.getTerritories()) {
            if(territory.getId() == id) {
                return territory;
            }
        }
        return null;
    }

    public void setArmiesToPlace() {
        int armies = riskModel.getCurrentPlayer().getTerritoriesOwned() / 3;

        int territoriesFoundAsia = 0;
        int territoriesFoundNorthAmerica = 0;
        int territoriesFoundEurope = 0;
        int territoriesFoundAfrica = 0;
        int territoriesFoundOceania = 0;
        int territoriesFoundSouthAmerica = 0;

        for(Territory territory: riskModel.getTerritories()) {
            switch (territory.continent) {
                case ASIA:
                    if(territory.getOccupier() == riskModel.getCurrentPlayer()) territoriesFoundAsia++;
                    break;
                case NORTH_AMERICA:
                    if(territory.getOccupier() == riskModel.getCurrentPlayer()) territoriesFoundNorthAmerica++;
                    break;
                case EUROPE:
                    if(territory.getOccupier() == riskModel.getCurrentPlayer()) territoriesFoundEurope++;
                    break;
                case AFRICA:
                    if(territory.getOccupier() == riskModel.getCurrentPlayer()) territoriesFoundAfrica++;
                    break;
                case OCEANIA:
                    if(territory.getOccupier() == riskModel.getCurrentPlayer()) territoriesFoundOceania++;
                    break;
                case SOUTH_AMERICA:
                    if(territory.getOccupier() == riskModel.getCurrentPlayer()) territoriesFoundSouthAmerica++;
                    break;
            }
        }

        //if owning a whole continent, add corresponding  armies amounts:
        if(territoriesFoundAsia == 12) {
            armies += 7;
        }
        if(territoriesFoundNorthAmerica == 9) {
            armies += 5;
        }
        if(territoriesFoundEurope == 7) {
            armies += 5;
        }
        if(territoriesFoundAfrica == 6) {
            armies += 3;
        }
        if(territoriesFoundOceania == 4) {
            armies += 2;
        }
        if(territoriesFoundSouthAmerica == 4) {
            armies += 2;
        }

       riskModel.getCurrentPlayer().giveArmies(armies);
    }
}