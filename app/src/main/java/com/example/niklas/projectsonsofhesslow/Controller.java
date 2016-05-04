package com.example.niklas.projectsonsofhesslow;

import android.support.annotation.Nullable;
import android.view.View;

import Graphics.GL_TouchEvent;
import Graphics.GL_TouchListener;
import Graphics.GraphicsManager;
import Graphics.MyGLSurfaceView;

public class Controller implements GL_TouchListener {
    private Risk riskModel;

    private enum GamePhase {PICK_TERRITORIES, PLACE_ARMIES, FIGHT}
    private GamePhase gamePhase = GamePhase.PICK_TERRITORIES;

    private int currentPlayerTracker = 0; //used to set next player
    private int territoriesPicked = 0;
    private OverlayController overlayController;
    private MyGLSurfaceView graphicsView;

    public Controller(OverlayController overlayController, MyGLSurfaceView graphicsView) {
        int territoryCount = GraphicsManager.getNumberOfTerritories();
        this.overlayController = overlayController;
        this.graphicsView = graphicsView;
        riskModel = new Risk(2, territoryCount); //somehow set number of players (2)
        //view observer thing?

        riskModel.setCurrentPlayer(riskModel.getPlayers()[0]);

        //set neighbours and continent
        for(int i = 0; i < territoryCount; i++) {
            Integer[] ids = GraphicsManager.getNeighbours(i);
            int number = ids.length; //number of neighbours
            Territory[] neighbours = new Territory[number];

            //set neighbours
            for(int k = 0; k < number; k++) {
                neighbours[k] = getTerritoryById(ids[k]);
            }
            riskModel.getTerritories()[i].setNeighbours(neighbours);

            //set continent
            riskModel.getTerritories()[i].setContinent(GraphicsManager.getContinetId(i));
        }
        overlayController.replaceText(R.id.playerTurn,"Player: " + riskModel.getCurrentPlayer().getName());
    }

    public void Handle(GL_TouchEvent event) {
        if(event.touchedRegion) {
            Territory touchedTerritory = getTerritoryById(event.regionId);
            switch(gamePhase) {
                case PICK_TERRITORIES:
                    if(touchedTerritory.getOccupier() == null) {
                        touchedTerritory.setOccupier(riskModel.getCurrentPlayer());
                        touchedTerritory.setArmyCount(1);
                        territoriesPicked++;
                        nextPlayer();
                        if(territoriesPicked == 42) {
                            gamePhase = GamePhase.PLACE_ARMIES;
                            setArmiesToPlace();
                        }
                    }
                    break;

                case PLACE_ARMIES:
                    if(touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()){
                        overlayController.addViewChange(R.layout.activity_placearmies);
                        overlayController.setBarMaxValue(R.id.seekBar,riskModel.getCurrentPlayer().getArmiesToPlace());
                        overlayController.replaceText(R.id.troopsSelected,"0");
                        overlayController.replaceText(R.id.troopsLeft,""+riskModel.getCurrentPlayer().getArmiesToPlace());
                        riskModel.setSelectedTerritory(touchedTerritory);
                    }
                    /*if(touchedTerritory.getOccupier() == riskModel.getCurrentPlayer()) {
                        touchedTerritory.changeArmyCount(1);
                        riskModel.getCurrentPlayer().decArmiesToPlace();
                    }*/
                    if(riskModel.getCurrentPlayer().getArmiesToPlace() == 0) {
                        gamePhase = GamePhase.FIGHT;
                    }
                    break;

                case FIGHT:
                    if(touchedTerritory.getOccupier() == riskModel.getCurrentPlayer() && touchedTerritory.getArmyCount() > 1) {
                        //clear old possible defenders
                        riskModel.getDefenders().clear();
                        //checks if any neighboring territory can be attacked
                        for(Territory neighbour: touchedTerritory.getNeighbours()) {
                            if(neighbour.getOccupier() != riskModel.getCurrentPlayer()) {
                                riskModel.getDefenders().add(neighbour); //for view to show, maybe outline yellow or something?
                                riskModel.setAttackingTerritory(touchedTerritory);
                                riskModel.setDefendingTerritory(null);
                                overlayController.addViewChange(R.layout.activity_nextturn);
                            }
                        }
                    } else if (riskModel.getDefenders().contains(touchedTerritory)) {
                        riskModel.setDefendingTerritory(touchedTerritory);
                        overlayController.addViewChange(R.layout.activity_fightbutton);
                        //TODO show attack button
                    }
            }
        }
    }

    public void fightButtonPressed() {
        Die.fight(riskModel.getAttackingTerritory(), riskModel.getDefendingTerritory());
        if(riskModel.getDefendingTerritory().getOccupier() == riskModel.getCurrentPlayer()) {
            overlayController.addViewChange(R.layout.activity_nextturn);
        }
        graphicsView.requestRender();
    }

    public void nextTurn() {
        if(gamePhase == GamePhase.FIGHT) {
            riskModel.setAttackingTerritory(null);
            riskModel.setDefendingTerritory(null);
            nextPlayer();
            gamePhase = GamePhase.PLACE_ARMIES;
        }
    }

    private void nextPlayer() {
        currentPlayerTracker++;
        overlayController.replaceText(R.id.playerTurn,"Player: " + riskModel.getCurrentPlayer().getName());
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
            switch (territory.getContinent()) {
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
        System.out.println(armies);
    }
    public void placeButtonPressed(){
        Territory territory = riskModel.getSelectedTerritory();
        territory.setArmyCount(territory.getArmyCount() + overlayController.getBarValue(R.id.seekBar));
        riskModel.getCurrentPlayer().decArmiesToPlace(overlayController.getBarValue(R.id.seekBar));
        System.out.println("Amount: " + overlayController.getBarValue(R.id.seekBar));
        overlayController.setBarMaxValue(R.id.seekBar,riskModel.getCurrentPlayer().getArmiesToPlace());
        overlayController.replaceText(R.id.troopsLeft,""+riskModel.getCurrentPlayer().getArmiesToPlace());
        graphicsView.requestRender();
        gamePhase = GamePhase.FIGHT;
    }
}
