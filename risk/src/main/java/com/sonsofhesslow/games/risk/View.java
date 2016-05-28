package com.sonsofhesslow.games.risk;

import android.content.res.Resources;

import com.sonsofhesslow.games.risk.graphics.GraphicsManager;
import com.sonsofhesslow.games.risk.model.Card;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

public class View implements Observer {

    final float[] red = {1, 0, 0, 1};
    final float[] black = {0, 0, 0, 1};
    final float[] green = {0, 1, 0, 1};
    final float[] blue = {0, 0, 1, 1};


    private final Risk risk;
    Map<Player, float[]> playerColors = new HashMap<>();
    Overlay overlayController;

    GraphicsManager manager;
    Resources resources;
    public View(final Risk risk, Overlay overlayController, Resources resources) {
        this.risk = risk;
        this.overlayController = overlayController;
        manager = manager.getInstance();
        this.resources =resources;
    }


    public float[] getColor(Player p) {
        return playerColors.get(p);
    }

    public void update(Observable obs, Object arg) {
        if (obs instanceof Player) {
            overlayController.populateGridView((ArrayList<Card>) arg);
        } else if (obs instanceof Risk) {
            Risk risk = (Risk) obs;
            if(overlayController.getOverlay().getChildCount() > 1){
                overlayController.populateListView(risk.getPlayers(), playerColors.values());
            }
            if (arg instanceof Risk.RiskChangeEvent) {
                /*
                RISK CHANGE EVENT
                 */
                Risk.RiskChangeEvent event = (Risk.RiskChangeEvent) arg;
                switch(event.eventType) {
                    case ATTACK:
                        if (event.oldTerritory != null) {
                            manager.setOutlineColor(event.oldTerritory.getId(), black);
                            manager.setHeight(event.oldTerritory.getId(), 0);
                        }
                        if (event.newTerritory != null) {
                            manager.setOutlineColor(event.newTerritory.getId(), blue);
                            manager.setHeight(event.newTerritory.getId(), 0.04f);
                        }
                        if (event.newTerritory == null) {
                            //overlayController.setGamePhase(Risk.GamePhase.FIGHT);
                        }
                        break;

                    case DEFENCE:
                        if (event.oldTerritory != null) {
                            manager.removeAllArrows();
                            manager.setOutlineColor(event.oldTerritory.getId(), black);
                            manager.setHeight(event.oldTerritory.getId(), 0);
                        }
                        if (event.newTerritory != null) {
                            if(risk.getAttackingTerritory()!=null)
                            {
                                int overlayColorInt = resources.getColor(R.color.overlayFightColor);
                                float[] overlayColorF = Util.getFloatFromIntColor(overlayColorInt);
                                manager.addArrow(risk.getAttackingTerritory().getId(),event.newTerritory.getId(),-1,overlayColorF);
                            }
                            manager.setOutlineColor(event.newTerritory.getId(), red);
                            manager.setHeight(event.newTerritory.getId(), 0.04f);
                            overlayController.setFightVisible(true);
                        }
                        if (event.newTerritory == null) {

                        }
                        break;

                    case SELECTED:
                        if (event.oldTerritory != null) {
                            manager.setOutlineColor(event.oldTerritory.getId(), black);
                            manager.setHeight(event.oldTerritory.getId(), 0);
                        }
                        if (event.newTerritory != null) {
                            manager.setOutlineColor(event.newTerritory.getId(), blue);
                            manager.setHeight(event.newTerritory.getId(), 0.04f);
                            if (risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                                overlayController.setPlaceArmiesVisible(true);
                                overlayController.setBarMaxValue(risk.getCurrentPlayer().getArmiesToPlace());
                            }
                        }
                        if (event.newTerritory == null) {
                            if (risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                                overlayController.setGamePhase(Risk.GamePhase.MOVEMENT);
                            } else if(risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES){
                                overlayController.setPlaceArmiesVisible(true);
                                overlayController.setBarMaxValue(risk.getCurrentPlayer().getArmiesToPlace());
                            }
                        }
                        break;

                    case SECOND_SELECTED:
                        if (event.oldTerritory != null) {
                            manager.removeAllArrows();
                            manager.setOutlineColor(event.oldTerritory.getId(), black);
                            manager.setHeight(event.oldTerritory.getId(), 0);
                        }

                        if (event.newTerritory != null) {
                            if(risk.getSelectedTerritory()!=null)
                            {
                                int overlayColorInt = resources.getColor(R.color.overlayMovementColor);
                                float[] overlayColorF = Util.getFloatFromIntColor(overlayColorInt);
                                manager.addArrow(risk.getSelectedTerritory().getId(), event.newTerritory.getId(), -1, overlayColorF);
                            }
                            manager.setOutlineColor(event.newTerritory.getId(), green);
                            manager.setHeight(event.newTerritory.getId(), 0.04f);
                            overlayController.setPlaceArmiesVisible(true);
                            if(risk.getSelectedTerritory().getJustMovedArmies() == 0) {
                                overlayController.setBarMaxValue(risk.getSelectedTerritory().getArmyCount() - risk.getSelectedTerritory().getJustMovedArmies() - 1);
                            } else {
                                overlayController.setBarMaxValue(risk.getSelectedTerritory().getArmyCount() - risk.getSelectedTerritory().getJustMovedArmies());
                            }
                            // indication
                        }
                        if (event.newTerritory == null && risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                            overlayController.setGamePhase(Risk.GamePhase.MOVEMENT);
                        }
                        break;
                }
            } else if (arg instanceof Player) {
                /*
                PLAYER CHANGE EVENT
                 */
                Player event = (Player) arg;
                if (playerColors.get(event) != null) {
                    overlayController.setCurrentPlayer(event, Util.getIntFromColor(playerColors.get(event)));
                    overlayController.populateGridView(risk.getCurrentPlayer().getCards());
                }
                if (risk.getGamePhase() == Risk.GamePhase.PLACE_STARTING_ARMIES) {
                    overlayController.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
                    overlayController.setInformation("Armies to place: " + risk.getCurrentPlayer().getArmiesToPlace(), true);
                    //add some kind of indication to how many
                }
            } else if (arg instanceof Risk.GamePhase) {
                /*
                PHASE EVENT
                 */
                Risk.GamePhase event = (Risk.GamePhase) arg;
                if (event == Risk.GamePhase.PICK_TERRITORIES) {

                } else if (event == Risk.GamePhase.PLACE_STARTING_ARMIES) {
                    // Hide all
                    overlayController.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
                    overlayController.setInformation("Armies to place: " + risk.getCurrentPlayer().getArmiesToPlace(), true);
                    overlayController.setNextTurnName("Next Phase");
                } else if (event == Risk.GamePhase.PLACE_ARMIES) {
                    // Hide all
                    overlayController.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
                    overlayController.setPlaceArmiesVisible(true);
                    overlayController.setBarMaxValue(risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.setNextTurnName("Next Phase");
                    //add some kind of indication to how many
                } else if (event == Risk.GamePhase.FIGHT) {
                    overlayController.setGamePhase(Risk.GamePhase.FIGHT);
                    overlayController.setNextTurnVisible(true);
                } else if (event == Risk.GamePhase.MOVEMENT) {
                    overlayController.setGamePhase(Risk.GamePhase.MOVEMENT);
                    overlayController.setNextTurnVisible(true);
                    overlayController.setNextTurnName("Next Turn");
                }
            } else {
                /*
                PLACE EVENT
                 */
                if (risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                    overlayController.setBarMaxValue(risk.getCurrentPlayer().getArmiesToPlace());
                    if (risk.getCurrentPlayer().getArmiesToPlace() == 0) {
                        overlayController.setGamePhase(Risk.GamePhase.FIGHT);
                    }
                } else if (risk.getGamePhase() == Risk.GamePhase.MOVEMENT && risk.getSecondSelectedTerritory() != null) {
                    overlayController.setBarMaxValue(risk.getSelectedTerritory().getArmyCount() - risk.getSelectedTerritory().getJustMovedArmies() - 1);
                } else {
                    overlayController.setBarMaxValue(0);
                }
            }

        } else if (obs instanceof Territory) {
            Territory territory = (Territory) obs;
            if (arg instanceof Integer) {
                /*
                ARMY CHANGE EVENT
                 */
                int event = (Integer) arg;
                manager.setArmies(territory.getId(), event);
            } else if (arg instanceof Player) {
                /*
                OCCUPIER CHANGE EVENT
                 */
                Player event = (Player) arg;
                if (!playerColors.containsKey(event)) {
                    Player[] players = risk.getPlayers();
                    Random random = new Random(players[playerColors.size()].getParticipantId() +
                            (players[0].getParticipantId() == players[1].getParticipantId() ?
                                    new Random().nextInt(1000) :         //multiplayer - have same color across all units
                                    playerColors.size()));             //singleplayer - have different color every time
                    float[] rndColor = {random.nextFloat(), random.nextFloat(), random.nextFloat(), 1};
                    playerColors.put(event, rndColor);
                }
                if (risk.getAttackingTerritory() != null) {
                    manager.setColor(territory.getId(), playerColors.get(event), risk.getAttackingTerritory().getId());
                } else {
                    manager.setColor(territory.getId(), playerColors.get(event));
                }
            }
        }
    }
}
