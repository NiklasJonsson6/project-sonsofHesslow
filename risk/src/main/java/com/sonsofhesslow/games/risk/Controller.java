package com.sonsofhesslow.games.risk;

import android.support.annotation.Nullable;

import com.sonsofhesslow.games.risk.graphics.GL_TouchEvent;
import com.sonsofhesslow.games.risk.graphics.GL_TouchListener;
import com.sonsofhesslow.games.risk.graphics.GraphicsManager;

import java.util.Random;

public class Controller implements GL_TouchListener {
    private static Risk riskModel;
    private View riskView;

    private int currentPlayerTracker = 0; //used to set next player
    private int territoriesPicked = 0;
    private int self_id;
    private boolean territoryTaken = false;

    public Controller(int[] playerIds, int self_id) {
        this.self_id = self_id;
        int territoryCount = GraphicsManager.getNumberOfTerritories();
        riskModel = new Risk(playerIds, territoryCount); //somehow set number of players (2)
        riskView = new View(riskModel);
        riskModel.addObserver(riskView);

        riskModel.setCurrentPlayer(riskModel.getPlayers()[0]);

        //set neighbours and continent
        for (int i = 0; i < territoryCount; i++) {
            Integer[] ids = GraphicsManager.getNeighbours(i);
            int number = ids.length; //number of neighbours
            Territory[] neighbours = new Territory[number];

            //set neighbours
            for (int k = 0; k < number; k++) {
                neighbours[k] = getTerritoryById(ids[k]);
            }
            riskModel.getTerritories()[i].setNeighbours(neighbours);

            //set continent
            riskModel.getTerritories()[i].setContinent(GraphicsManager.getContinetId(i));
        }

        //give initial armies
        setStartingArmies();
    }

    public void handle(GL_TouchEvent event) {
        if (event.touchedRegion) {
            Territory touchedTerritory = getTerritoryById(event.regionId);
            if (self_id == riskModel.getPlayers()[currentPlayerTracker].getParticipantId()) {
                switch (riskModel.getGamePhase()) {
                    case PICK_TERRITORIES:
                        System.out.println("pick territories phase");
                        if (touchedTerritory.getOccupier() == null) {
                            touchedTerritory.setOccupier(riskModel.getCurrentPlayer());

                            //for debugging only
                            Random r = new Random();
                            final int EXTRA_TRIES = 0;
                            for(int i = 0; i < EXTRA_TRIES; i++) {
                                int randomNumber = r.nextInt(42);
                                Territory randomTerritory = getTerritoryById(randomNumber);
                                if(randomTerritory.getOccupier() == null) {
                                    randomTerritory.setArmyCount(1);
                                    randomTerritory.setOccupier(riskModel.getCurrentPlayer());
                                } else{
                                    i--;    //find a new territory to place
                                }
                            }

                            touchedTerritory.setArmyCount(1);
                            territoriesPicked++;
                            riskModel.getCurrentPlayer().decArmiesToPlace();
                            // TODO: 2016-05-16 better solution?
                            boolean canContinueToPlacePhase = true;
                            for(Territory territory : riskModel.getTerritories()){
                                if(territory.getOccupier() == null){
                                    canContinueToPlacePhase = false;        //one territory with no occupier found
                                    break;
                                }
                            }

                            if (canContinueToPlacePhase) {
                                riskModel.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
                            }
                            nextPlayer();
                        }
                        break;

                    case PLACE_STARTING_ARMIES:
                        System.out.println("place starting armies phase");
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                            touchedTerritory.changeArmyCount(1);
                            System.out.println(riskModel.getCurrentPlayer().getArmiesToPlace());
                            riskModel.getCurrentPlayer().decArmiesToPlace();
                            if (riskModel.getCurrentPlayer().getArmiesToPlace() == 0) {
                                System.out.println("current game phase: " + riskModel.getGamePhase());
                                System.out.println("Should switch to fight phase");
                                riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                            }
                            nextPlayer();
                        }
                        break;

                    case PLACE_ARMIES:
                        System.out.println("Place Phase");
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                            //armies are palced with a slider, triggered by listener
                            riskModel.setSelectedTerritory(touchedTerritory);
                        }
                        /*if(touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                            touchedTerritory.changeArmyCount(1);
                            riskModel.getCurrentPlayer().decArmiesToPlace();
                        }*/
                        /*if(riskModel.getCurrentPlayer().getArmiesToPlace() == 0) {
                            gamePhase = GamePhase.FIGHT;
                            overlayController.addViewChange(R.layout.activity_nextturn);
                        }*/
                        break;

                    case FIGHT:
                        System.out.println("fight phase");
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()
                                && touchedTerritory.getArmyCount() > 1) {
                            //clear old possible defenders
                            riskModel.getDefenders().clear();
                            //checks if any neighboring territory can be attacked
                            for (Territory neighbour : touchedTerritory.getNeighbours()) {
                                if (neighbour.getOccupier() != riskModel.getCurrentPlayer()) {
                                    riskModel.getDefenders().add(neighbour);
                                    riskModel.setAttackingTerritory(touchedTerritory);
                                    riskModel.setDefendingTerritory(null);
                                }
                            }
                        } else if (riskModel.getDefenders().contains(touchedTerritory)
                                && riskModel.getAttackingTerritory() != null) {
                            riskModel.setDefendingTerritory(touchedTerritory);
                            //TODO show attack button
                        }
                        break;

                    case MOVEMENT:
                        System.out.println("movement phase");
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()
                                && touchedTerritory.getArmyCount() > 1
                                && riskModel.getSelectedTerritory() == null) {
                            //clear old possible defenders
                            riskModel.getNeighbors().clear();
                            //checks if any neighboring territory can be attacked
                            riskModel.setSelectedTerritory(touchedTerritory);
                            for (Territory neighbour : touchedTerritory.getNeighbours()) {
                                if (neighbour.getOccupier() == riskModel.getCurrentPlayer()) {
                                    riskModel.getNeighbors().add(neighbour);
                                    riskModel.setSecondSelectedTerritory(null);
                                }
                            }
                        } else if (riskModel.getNeighbors().contains(touchedTerritory)
                                && riskModel.getSelectedTerritory() != null) {
                            riskModel.setSecondSelectedTerritory(touchedTerritory);
                            //TODO show attack button
                        }
                        break;
                }
            }
        }
    }

    public void fightButtonPressed() {
        Die.fight(riskModel.getAttackingTerritory(), riskModel.getDefendingTerritory());
        if (riskModel.getDefendingTerritory().getOccupier() == riskModel.getCurrentPlayer()) {
            territoryTaken = true;
            riskModel.getAttackingTerritory().changeArmyCount(-1);
            riskModel.getDefendingTerritory().changeArmyCount(+1);
            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
        } else if (riskModel.getAttackingTerritory().getArmyCount() < 2) {
            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
        }
        GraphicsManager.requestRender();
    }

    public void nextTurn() {
        if (riskModel.getGamePhase() == Risk.GamePhase.MOVEMENT) {
            riskModel.setSelectedTerritory(null);
            riskModel.setSecondSelectedTerritory(null);
            nextPlayer();
            riskModel.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
        }
        if (riskModel.getGamePhase() == Risk.GamePhase.FIGHT) {
            riskModel.setGamePhase(Risk.GamePhase.MOVEMENT);
            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
            riskModel.setSelectedTerritory(null);
        }
    }

    public void nextPlayer() {
        //progress currentplayertracker
        currentPlayerTracker++;
        if (currentPlayerTracker == riskModel.getPlayers().length) currentPlayerTracker = 0;

        //give card
        if(territoryTaken) {
            riskModel.getCurrentPlayer().giveOneCard();
            territoryTaken = false;
        }

        //next player, change gamephase
        riskModel.setCurrentPlayer(riskModel.getPlayers()[currentPlayerTracker]);
        if (riskModel.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES) {
            riskModel.getCurrentPlayer().giveArmies(1);
        } else if (riskModel.getGamePhase() != Risk.GamePhase.PLACE_STARTING_ARMIES) {
            setArmiesToPlace();
        }

        if(riskModel.getPlayers()[0].getParticipantId() != riskModel.getPlayers()[1].getParticipantId() && riskModel.getCurrentPlayer().getParticipantId() != self_id) {
            //multiplayer
            System.out.println("multiplayer@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
            MainActivity.getOverlayController().addViewChange(R.layout.activity_wait);
        }
    }

    @Nullable
    public static Territory getTerritoryById(int id) {
        for (Territory territory : riskModel.getTerritories()) {
            if (territory.getId() == id) {
                return territory;
            }
        }
        return null;
    }

    private void setStartingArmies() {
        //rules from hasbro
        for (Player player : riskModel.getPlayers()) {
            player.giveArmies(40 / riskModel.getPlayers().length);
        }
    }

    public void setArmiesToPlace() {
        int armies = riskModel.getCurrentPlayer().getTerritoriesOwned() / 3;

        int territoriesFoundAsia = 0;
        int territoriesFoundNorthAmerica = 0;
        int territoriesFoundEurope = 0;
        int territoriesFoundAfrica = 0;
        int territoriesFoundOceania = 0;
        int territoriesFoundSouthAmerica = 0;

        for (Territory territory : riskModel.getTerritories()) {
            switch (territory.getContinent()) {
                case ASIA:
                    if (territory.getOccupier() == riskModel.getCurrentPlayer())
                        territoriesFoundAsia++;
                    break;
                case NORTH_AMERICA:
                    if (territory.getOccupier() == riskModel.getCurrentPlayer())
                        territoriesFoundNorthAmerica++;
                    break;
                case EUROPE:
                    if (territory.getOccupier() == riskModel.getCurrentPlayer())
                        territoriesFoundEurope++;
                    break;
                case AFRICA:
                    if (territory.getOccupier() == riskModel.getCurrentPlayer())
                        territoriesFoundAfrica++;
                    break;
                case OCEANIA:
                    if (territory.getOccupier() == riskModel.getCurrentPlayer())
                        territoriesFoundOceania++;
                    break;
                case SOUTH_AMERICA:
                    if (territory.getOccupier() == riskModel.getCurrentPlayer())
                        territoriesFoundSouthAmerica++;
                    break;
            }
        }

        //if owning a whole continent, add corresponding  armies amounts:
        if (territoriesFoundAsia == 12) {
            armies += 7;
        }
        if (territoriesFoundNorthAmerica == 9) {
            armies += 5;
        }
        if (territoriesFoundEurope == 7) {
            armies += 5;
        }
        if (territoriesFoundAfrica == 6) {
            armies += 3;
        }
        if (territoriesFoundOceania == 4) {
            armies += 2;
        }
        if (territoriesFoundSouthAmerica == 4) {
            armies += 2;
        }

        riskModel.getCurrentPlayer().giveArmies(armies);
        System.out.println(armies);
    }

    public void placeButtonPressed(int seekBarValue) {
        if (riskModel.getGamePhase() == Risk.GamePhase.PLACE_ARMIES
                && riskModel.getSelectedTerritory() != null) {
            //riskModel.placeEvent();
            Territory territory = riskModel.getSelectedTerritory();
            territory.changeArmyCount(seekBarValue);
            riskModel.getCurrentPlayer().decArmiesToPlace(seekBarValue);
            if (riskModel.getCurrentPlayer().getArmiesToPlace() == 0
                    && riskModel.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                System.out.println("In fight");
                riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                riskModel.setSelectedTerritory(null);
            }
            riskModel.placeEvent();
        } else if (riskModel.getGamePhase() == Risk.GamePhase.MOVEMENT
                && riskModel.getSelectedTerritory() != null
                && riskModel.getSecondSelectedTerritory() != null) {
            Territory from = riskModel.getSelectedTerritory();
            Territory to = riskModel.getSecondSelectedTerritory();
            to.changeArmyCount(seekBarValue);
            from.changeArmyCount(-seekBarValue);
            riskModel.placeEvent();
        }
    }

    public void doneButtonPressed() {
        riskModel.setSelectedTerritory(null);
        riskModel.setSecondSelectedTerritory(null);
    }

    public void refreshGamePhase() {
        if (riskModel.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES) {
            boolean canContinueToPlacePhase = true;
            for (Territory territory : riskModel.getTerritories()) {
                if (territory.getOccupier() == null) {
                    canContinueToPlacePhase = false;        //one territory with no occupier found
                    break;
                }
            }

            if (canContinueToPlacePhase) {
                riskModel.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
            }
        }
    }

    public static Risk getRiskModel() {
        return riskModel;
    }
}
