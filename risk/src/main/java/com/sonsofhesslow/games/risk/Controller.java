package com.sonsofhesslow.games.risk;

import android.support.annotation.Nullable;

import com.sonsofhesslow.games.risk.graphics.GL_TouchEvent;
import com.sonsofhesslow.games.risk.graphics.GL_TouchListener;
import com.sonsofhesslow.games.risk.graphics.GraphicsManager;
import com.sonsofhesslow.games.risk.model.Card;
import com.sonsofhesslow.games.risk.model.Die;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Random;

public class Controller implements GL_TouchListener {
    private static final int TERRITORIES_IN_ASIA = 12;
    private static final int TERRITORIES_IN_NORTH_AMERICA = 9;
    private static final int TERRITORIES_IN_EUROPE = 7;
    private static final int TERRITORIES_IN_AFRICA = 6;
    private static final int TERRITORIES_IN_OCEANIA = 4;
    private static final int TERRITORIES_IN_SOUTH_AMERICA = 4;

    private static final int EXTRA_TROOPS_ASIA = 7;
    private static final int EXTRA_TROOPS_NORTH_AMERICA = 5;
    private static final int EXTRA_TROOPS_EUROPE = 5;
    private static final int EXTRA_TROOPS_AFRICA = 3;
    private static final int EXTRA_TROOPS_OCEANIA = 2;
    private static final int EXTRA_TROOPS_SOUTH_AMERICA = 2;

    private static Risk riskModel;

    private int currentPlayerIndex = 0; //used to set next player
    private int selfId;
    private boolean territoryTaken = false;
    private Overlay overlayController;
    private ArrayList<Territory> movementChangedTerritories = new ArrayList<>();
    private View riskView = null;

    public Controller(int[] playerIds, Overlay overlayController) {
        this.selfId = 0;
        this.overlayController = overlayController;
        int territoryCount = GraphicsManager.getInstance().getNumberOfTerritories();
        riskModel = new Risk(playerIds, territoryCount); //somehow set number of players (2)

        riskModel.setCurrentPlayer(riskModel.getPlayers()[0]);

        riskView = new View(riskModel);

        //add observers
        riskModel.addObserver(riskView);
        for(Territory territory: riskModel.getTerritories()) {
            territory.addObserver(riskView);
        }

        //set neighbours and continent
        for (int i = 0; i < territoryCount; i++) {
            Integer[] ids = GraphicsManager.getInstance().getNeighbours(i);
            int number = ids.length; //number of neighbours
            Territory[] neighbours = new Territory[number];

            //set neighbours
            for (int k = 0; k < number; k++) {
                neighbours[k] = getTerritoryById(ids[k]);
            }
            riskModel.getTerritories()[i].setNeighbours(neighbours);

            //set continent
            riskModel.getTerritories()[i].setContinent(GraphicsManager.getInstance().getContinetId(i));
        }

        if(!isOnline()){
            //if it's an online game this will be done later, after selfId is set
            setStartingArmies();
        }
    }

    public void handle(GL_TouchEvent event) {
        System.out.println("handle gl event");
        if (event.touchedRegion) {
            Territory touchedTerritory = getTerritoryById(event.regionId);
            if (selfId == riskModel.getPlayers()[currentPlayerIndex].getParticipantId()) {
                switch (riskModel.getGamePhase()) {
                    case PICK_TERRITORIES:
                        System.out.println("pick territories phase");
                        if (touchedTerritory.getOccupier() == null) {
                            touchedTerritory.setOccupier(riskModel.getCurrentPlayer());

                            //for debugging only
                            Random r = new Random();
                            final int EXTRA_TRIES = 20;
                            for(int i = 0; i < EXTRA_TRIES; i++) {
                                int randomNumber = r.nextInt(42);
                                Territory randomTerritory = getTerritoryById(randomNumber);
                                if(randomTerritory.getOccupier() == null) {
                                    randomTerritory.setArmyCount(1);
                                    randomTerritory.setOccupier(riskModel.getCurrentPlayer());
                                    riskModel.getCurrentPlayer().decArmiesToPlace();
                                } else{
                                    i--;    //find a new territory to place
                                }
                            }

                            touchedTerritory.setArmyCount(1);
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
                            riskModel.getCurrentPlayer().decArmiesToPlace();
                            if (selfId != 0 && riskModel.getCurrentPlayer().getArmiesToPlace() == 0) {
                                //multiplayer
                                riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                            }  else {
                                boolean playerHasArmiesLeft = false;
                                for(Player player : riskModel.getPlayers()) {
                                    if(player.getArmiesToPlace() > 0) {
                                        playerHasArmiesLeft = true;
                                        break;
                                    }
                                }
                                if(!playerHasArmiesLeft) {
                                    riskModel.setGamePhase(Risk.GamePhase.FIGHT);
                                }
                            }
                            nextPlayer();
                        }
                        break;

                    case PLACE_ARMIES:
                        System.out.println("Place Phase");
                        if (touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                            // TODO: 2016-05-26 armies are placed with a slider, triggered by listener
                            riskModel.setSelectedTerritory(touchedTerritory);
                        }
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
        GraphicsManager.getInstance().requestRender();
    }

    public void nextTurn() {
        if (riskModel.getGamePhase() == Risk.GamePhase.MOVEMENT) {
            riskModel.setSelectedTerritory(null);
            riskModel.setSecondSelectedTerritory(null);
            refreshMovementChangedTerritories();
            nextPlayer();
            riskModel.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
        }
        if (riskModel.getGamePhase() == Risk.GamePhase.FIGHT) {
            if (!playerCanMove(riskModel.getCurrentPlayer())) {
                riskModel.setGamePhase(Risk.GamePhase.MOVEMENT);
            } else {
                riskModel.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
                nextPlayer();
                // TODO: 2016-05-24 notify player 
            }

            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
            riskModel.setSelectedTerritory(null);
        }
    }

    private boolean playerCanMove(Player player) {
        ArrayList<Territory> playersTerritories = new ArrayList<>();
        for(Territory territory : riskModel.getTerritories()) {
            if(territory.getOccupier().equals(player)){
                playersTerritories.add(territory);
            }
        }

        for(Territory territory : playersTerritories) {
            if(territory.getArmyCount() > 1){
                //cannot move
                return false;
            }
        }

        //can move, there is atleast one territory with more than 1 army
        return true;
    }

    public void nextPlayer() {
        int playerSearchIndex = currentPlayerIndex;
        
        boolean nextPlayerIndexFound = false;

        while (!nextPlayerIndexFound){
            playerSearchIndex++;
            if (playerSearchIndex == riskModel.getPlayers().length) {
                playerSearchIndex = 0;
            }

            Player playerToTest = riskModel.getPlayers()[playerSearchIndex];

            //check if player is alive
            if(riskModel.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES) {
                nextPlayerIndexFound = true;
            } else if(playerToTest.isAlive() ) {
                for (Territory territory : riskModel.getTerritories()) {
                    if (territory.getOccupier().getParticipantId() == playerToTest.getParticipantId()) {
                        //player is occupier of atleast one territory, is alive
                        nextPlayerIndexFound = true;
                        break;
                    }
                }
                if(!nextPlayerIndexFound) {
                    //player is no longer alive
                    riskModel.getPlayers()[playerSearchIndex].setAlive(false);
                }
            }
        }

        if(playerSearchIndex == currentPlayerIndex) {
            //player won
            playerWon(riskModel.getPlayers()[currentPlayerIndex]);
        }

        //set next player
        currentPlayerIndex = playerSearchIndex;

        //gives armies for placement phase
        if(riskModel.getGamePhase() != Risk.GamePhase.PICK_TERRITORIES && riskModel.getGamePhase() != Risk.GamePhase.PLACE_STARTING_ARMIES) {
            setArmiesToPlace(riskModel.getPlayers()[currentPlayerIndex]);
        }
        
        if(territoryTaken) {
            riskModel.getCurrentPlayer().giveOneCard();
            territoryTaken = false;
        }

        //next player
        riskModel.setCurrentPlayer(riskModel.getPlayers()[currentPlayerIndex]);

        if(isOnline() && riskModel.getCurrentPlayer().getParticipantId() != selfId) {
            //multiplayer & not users turn
            overlayController.addView(R.layout.activity_wait);
        } else {
            overlayController.removeView(R.layout.activity_wait);
        }
        GraphicsManager.getInstance().requestRender();
    }

    private void playerWon(Player player) {
        System.out.println("player won@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        // TODO: 2016-05-20
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
        if (isOnline()) {
            for (Player player: riskModel.getPlayers()) {
                if (player.getParticipantId() == selfId) {
                    System.out.println("giving starting armies");
                    player.giveArmies(50 - (5*riskModel.getPlayers().length));
                }
            }
        } else {
            //singleplayer
            for (Player player: riskModel.getPlayers()) {
                player.giveArmies(50 - (5*riskModel.getPlayers().length));
            }
        }
    }

    public void setArmiesToPlace(Player player) {
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
                    if (territory.getOccupier().equals(player))
                        territoriesFoundAsia++;
                    break;
                case NORTH_AMERICA:
                    if (territory.getOccupier().equals(player))
                        territoriesFoundNorthAmerica++;
                    break;
                case EUROPE:
                    if (territory.getOccupier().equals(player))
                        territoriesFoundEurope++;
                    break;
                case AFRICA:
                    if (territory.getOccupier().equals(player))
                        territoriesFoundAfrica++;
                    break;
                case OCEANIA:
                    if (territory.getOccupier().equals(player))
                        territoriesFoundOceania++;
                    break;
                case SOUTH_AMERICA:
                    if (territory.getOccupier().equals(player))
                        territoriesFoundSouthAmerica++;
                    break;
            }
        }

        //if owning a whole continent, add corresponding  armies amounts:
        if (territoriesFoundAsia == TERRITORIES_IN_ASIA) {
            armies += EXTRA_TROOPS_ASIA;
        }
        if (territoriesFoundNorthAmerica == TERRITORIES_IN_NORTH_AMERICA) {
            armies += EXTRA_TROOPS_NORTH_AMERICA;
        }
        if (territoriesFoundEurope == TERRITORIES_IN_EUROPE) {
            armies += EXTRA_TROOPS_EUROPE;
        }
        if (territoriesFoundAfrica == TERRITORIES_IN_AFRICA) {
            armies += EXTRA_TROOPS_AFRICA;
        }
        if (territoriesFoundOceania == TERRITORIES_IN_OCEANIA) {
            armies += EXTRA_TROOPS_OCEANIA;
        }
        if (territoriesFoundSouthAmerica == TERRITORIES_IN_SOUTH_AMERICA) {
            armies += EXTRA_TROOPS_SOUTH_AMERICA;
        }

        player.giveArmies(armies);
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
            //to prevent multiple movements for troops (each troop should only be able to move 1 step)
            to.setJustMovedArmies(seekBarValue);
            movementChangedTerritories.add(to);
            from.changeArmyCount(-seekBarValue);
            riskModel.placeEvent();
        }
        GraphicsManager.getInstance().requestRender();
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

    private void refreshMovementChangedTerritories() {
        for(Territory changedTerritory: movementChangedTerritories) {
            changedTerritory.setJustMovedArmies(0);
        }
    }

    public void setSelfId(int selfId) {
        this.selfId = selfId;

        //give initial armies
        setStartingArmies();
    }

    public boolean isOnline(){
        return riskModel.getPlayers()[0].getParticipantId() != riskModel.getPlayers()[1].getParticipantId();
    }
    public void turnInCards(ArrayList<Integer> selectedCards){
        Collections.sort(selectedCards);
        for(Integer inte: selectedCards){
            System.out.println("Index value: " + inte.intValue());
        }
        ArrayList<Card> temp = new ArrayList<Card>();
        temp.addAll(riskModel.getCurrentPlayer().getCards());
        temp.remove(selectedCards.get(2).intValue());
        temp.remove(selectedCards.get(1).intValue());
        temp.remove(selectedCards.get(0).intValue());
        riskModel.getCurrentPlayer().setCards(temp);
        riskView.updateCardView(riskModel);
    }
}
