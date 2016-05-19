package com.sonsofhesslow.games.risk;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import com.sonsofhesslow.games.risk.graphics.GraphicsManager;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

/**
 * Created by Daniel on 27/04/2016.
 */
public class View implements Observer {

    final float[] red = {1, 0, 0, 1};
    final float[] black = {0, 0, 0, 1};
    final float[] green = {0, 1, 0, 1};
    final float[] blue = {0, 0, 1, 1};


    final Risk risk;
    Map<Player, float[]> playerColors = new HashMap<>();
    Overlay overlayController;

    public View(final Risk risk) {
        this.risk = risk;
        overlayController = MainActivity.newOverlayController;
        /* for (Territory t : risk.getTerritories()) {
            t.addOccupierListeners(new Territory.OccupierChangeListener() {
                @Override
                public void handle(Territory.OccupierChangeEvent occupierChangeEvent) {
                    if (!playerColors.containsKey(occupierChangeEvent.newValue)) {
                        Player[] players = Controller.getRiskModel().getPlayers();
                        Random random = new Random(players[playerColors.size()].getParticipantId() +
                                (players[0].getParticipantId() == players[1].getParticipantId() ?
                                        new Random().nextInt(1000) :         //multiplayer - have same color across all units
                                        playerColors.size()));             //singleplayer - have different color every time
                        float[] rndColor = {random.nextFloat(), random.nextFloat(), random.nextFloat(), 1};
                        playerColors.put(occupierChangeEvent.newValue, rndColor);
                    }
                    if (risk.getAttackingTerritory() != null) {
                        GraphicsManager.setColor(occupierChangeEvent.territory.getId(), playerColors.get(occupierChangeEvent.newValue), risk.getAttackingTerritory().getId());
                    } else {
                        GraphicsManager.setColor(occupierChangeEvent.territory.getId(), playerColors.get(occupierChangeEvent.newValue));
                    }
                }
            });

            t.addArmyListeners(new Territory.ArmyChangeListener() {
                @Override
                public void handle(Territory.ArmyChangeEvent armyChangeEvent) {
                    GraphicsManager.setArmies(armyChangeEvent.territory.getId(), armyChangeEvent.newValue);
                }
            });

        }
        risk.addAttackListener(new Risk.RiskEventListener() {
            @Override
            public void changeEvent(Risk.RiskChangeEvent riskChangeEvent) {
                if (riskChangeEvent.oldTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.oldTerritory.getId(), black);
                    GraphicsManager.setHeight(riskChangeEvent.oldTerritory.getId(), 0);
                }
                if (riskChangeEvent.newTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.newTerritory.getId(), blue);
                    GraphicsManager.setHeight(riskChangeEvent.newTerritory.getId(), 0.04f);
                }
                if (riskChangeEvent.newTerritory == null) {
                    overlayController.addViewChange(R.layout.activity_attackphase);
                    overlayController.replaceText(R.id.nextTurnButton, "Next Phase");
                }

            }
        });

        risk.addDefenceListeners(new Risk.RiskEventListener() {
            @Override
            public void changeEvent(Risk.RiskChangeEvent riskChangeEvent) {
                if (riskChangeEvent.oldTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.oldTerritory.getId(), black);
                    GraphicsManager.setHeight(riskChangeEvent.oldTerritory.getId(), 0);
                }
                if (riskChangeEvent.newTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.newTerritory.getId(), red);
                    GraphicsManager.setHeight(riskChangeEvent.newTerritory.getId(), 0.04f);
                    overlayController.addViewChange(R.layout.activity_fightbutton);
                }
                if (riskChangeEvent.newTerritory == null) {
                    overlayController.addViewChange(R.layout.activity_attackphase);
                }
            }
        });
        risk.addSelectedListeners(new Risk.RiskEventListener() {
            @Override
            public void changeEvent(Risk.RiskChangeEvent riskChangeEvent) {
                if (riskChangeEvent.oldTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.oldTerritory.getId(), black);
                    GraphicsManager.setHeight(riskChangeEvent.oldTerritory.getId(), 0);
                }
                if (riskChangeEvent.newTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.newTerritory.getId(), blue);
                    GraphicsManager.setHeight(riskChangeEvent.newTerritory.getId(), 0.04f);
                    if (risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                        overlayController.addViewChange(R.layout.activity_placearmies);
                        overlayController.setBarMaxValue(R.id.seekBar, risk.getCurrentPlayer().getArmiesToPlace());
                        overlayController.replaceText(R.id.troopsSelected, "0");
                        overlayController.replaceText(R.id.troopsLeft, "" + risk.getCurrentPlayer().getArmiesToPlace());
                    }
                }
                if (riskChangeEvent.newTerritory == null) {
                    if (risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                        overlayController.addViewChange(R.layout.activity_movmentphase);
                    } else {
                        overlayController.addViewChange(R.layout.activity_placearmies);
                        overlayController.setBarMaxValue(R.id.seekBar, risk.getCurrentPlayer().getArmiesToPlace());
                        overlayController.replaceText(R.id.troopsSelected, "0");
                        overlayController.replaceText(R.id.troopsLeft, "" + risk.getCurrentPlayer().getArmiesToPlace());
                    }
                }
            }
        });
        risk.addSecondSelectedListeners(new Risk.RiskEventListener() {
            @Override
            public void changeEvent(Risk.RiskChangeEvent riskChangeEvent) {
                if (riskChangeEvent.oldTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.oldTerritory.getId(), black);
                    GraphicsManager.setHeight(riskChangeEvent.oldTerritory.getId(), 0);
                }

                if (riskChangeEvent.newTerritory != null) {
                    GraphicsManager.setOutlineColor(riskChangeEvent.newTerritory.getId(), green);
                    GraphicsManager.setHeight(riskChangeEvent.newTerritory.getId(), 0.04f);
                    overlayController.addViewChange(R.layout.activity_placearmies);
                    overlayController.setBarMaxValue(R.id.seekBar, risk.getSelectedTerritory().getArmyCount() - 1);
                    overlayController.replaceText(R.id.troopsLeft, "" + (risk.getSelectedTerritory().getArmyCount() - 1));
                }
                if (riskChangeEvent.newTerritory == null) {
                    overlayController.addViewChange(R.layout.activity_movmentphase);
                }
            }
        }); */
        risk.addOverlayListener(new OverlayChangeListener() {
            @Override
            public void phaseEvent(OverlayChangeEvent overlayChangeEvent) {
                System.out.println("Pleeease");
                System.out.println(overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES) {

                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_STARTING_ARMIES) {
                    // Hide all
                    overlayController.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
                    overlayController.setInformation("Armies to place: " + risk.getCurrentPlayer().getArmiesToPlace(), true);
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                    // Hide all
                    System.out.println("Fight Phase view");
                    overlayController.setGamePhase(Risk.GamePhase.PLACE_ARMIES);
                    overlayController.setPlaceArmiesVisible(true);
                    overlayController.setBarMaxValue(overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    //add some kind of indication to how many
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.FIGHT) {
                    overlayController.setGamePhase(Risk.GamePhase.FIGHT);
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                    overlayController.setGamePhase(Risk.GamePhase.MOVEMENT);
                }
            }

            @Override
            public void placeEvent(OverlayChangeEvent overlayChangeEvent) {
                if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                    overlayController.setBarMaxValue(overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    //add some kind of indication to how many
                    if (overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace() == 0) {
                        overlayController.setGamePhase(Risk.GamePhase.FIGHT);
                    }
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.MOVEMENT && overlayChangeEvent.risk.getSecondSelectedTerritory() != null) {
                    overlayController.setBarMaxValue(overlayChangeEvent.risk.getSelectedTerritory().getArmyCount() - 1);
                    //add some kind of indication to how many
                } else {
                    overlayController.setBarMaxValue(0);
                    //add some kind of indication to how many
                }
            }

            @Override
            public void playerChangeEvent(OverlayChangeEvent overlayChangeEvent) {
                if (playerColors.get(overlayChangeEvent.risk.getCurrentPlayer()) != null) {
                    overlayController.setCurrentPlayer(overlayChangeEvent.risk.getCurrentPlayer(), Util.getIntFromColor(playerColors.get(overlayChangeEvent.risk.getCurrentPlayer())));
                }
                if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_STARTING_ARMIES) {
                    overlayController.setGamePhase(Risk.GamePhase.PLACE_STARTING_ARMIES);
                    overlayController.setInformation("Armies to place: " + overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace(), true);
                    //add some kind of indication to how many
                }
            }
        });
    }

    public float[] getColor(Player p) {
        return playerColors.get(p);
    }

    public void update(Observable obs, Object arg) {
        System.out.println("update");
        if (obs instanceof Risk) {
            if (arg instanceof Risk.RiskChangeEvent) {
                Risk.RiskChangeEvent event = (Risk.RiskChangeEvent) arg;
                switch(event.eventType) {
                    case ATTACK:
                        if (event.oldTerritory != null) {
                            GraphicsManager.setOutlineColor(event.oldTerritory.getId(), black);
                            GraphicsManager.setHeight(event.oldTerritory.getId(), 0);
                        }
                        if (event.newTerritory != null) {
                            GraphicsManager.setOutlineColor(event.newTerritory.getId(), blue);
                            GraphicsManager.setHeight(event.newTerritory.getId(), 0.04f);
                        }
                        if (event.newTerritory == null) {
                            overlayController.setGamePhase(Risk.GamePhase.FIGHT);
                        }
                        break;

                    case DEFENCE:
                        if (event.oldTerritory != null) {
                            GraphicsManager.setOutlineColor(event.oldTerritory.getId(), black);
                            GraphicsManager.setHeight(event.oldTerritory.getId(), 0);
                        }
                        if (event.newTerritory != null) {
                            GraphicsManager.setOutlineColor(event.newTerritory.getId(), red);
                            GraphicsManager.setHeight(event.newTerritory.getId(), 0.04f);
                            overlayController.setFightVisible(true);
                        }
                        if (event.newTerritory == null) {
                            overlayController.setNextTurnVisible(true);
                        }
                        break;

                    case SELECTED:
                        if (event.oldTerritory != null) {
                            GraphicsManager.setOutlineColor(event.oldTerritory.getId(), black);
                            GraphicsManager.setHeight(event.oldTerritory.getId(), 0);
                        }
                        if (event.newTerritory != null) {
                            GraphicsManager.setOutlineColor(event.newTerritory.getId(), blue);
                            GraphicsManager.setHeight(event.newTerritory.getId(), 0.04f);
                            if (risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                                overlayController.setPlaceArmiesVisible(true);
                                overlayController.setBarMaxValue(risk.getCurrentPlayer().getArmiesToPlace());
                            }
                        }
                        if (event.newTerritory == null) {
                            if (risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                                overlayController.setGamePhase(Risk.GamePhase.MOVEMENT);
                            } else {
                                overlayController.setPlaceArmiesVisible(true);
                                overlayController.setBarMaxValue(risk.getCurrentPlayer().getArmiesToPlace());
                                //Indication
                            }
                        }
                        break;

                    case SECOND_SELECTED:
                        if (event.oldTerritory != null) {
                            GraphicsManager.setOutlineColor(event.oldTerritory.getId(), black);
                            GraphicsManager.setHeight(event.oldTerritory.getId(), 0);
                        }

                        if (event.newTerritory != null) {
                            GraphicsManager.setOutlineColor(event.newTerritory.getId(), green);
                            GraphicsManager.setHeight(event.newTerritory.getId(), 0.04f);
                            overlayController.setPlaceArmiesVisible(true);
                            overlayController.setBarMaxValue(risk.getSelectedTerritory().getArmyCount() - 1);
                            // indication
                        }
                        if (event.newTerritory == null) {
                            overlayController.setGamePhase(Risk.GamePhase.MOVEMENT);
                        }
                        break;
                }
            }

        } else if (obs instanceof Territory) {
            Territory territory = (Territory) obs;
            if (arg instanceof Integer) {
                int event = (Integer) arg;
                GraphicsManager.setArmies(territory.getId(), event);

            } else if (arg instanceof Player) {
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
                    GraphicsManager.setColor(territory.getId(), playerColors.get(event), risk.getAttackingTerritory().getId());
                } else {
                    GraphicsManager.setColor(territory.getId(), playerColors.get(event));
                }
            }
        }
    }
}
