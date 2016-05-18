package com.sonsofhesslow.games.risk;

import android.graphics.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Random;

import com.sonsofhesslow.games.risk.graphics.GraphicsManager;

/**
 * Created by Daniel on 27/04/2016.
 */
public class View implements Observer {

    final float[] red = {1, 0, 0, 1};
    final float[] black = {0, 0, 0, 1};
    final float[] green = {0, 1, 0, 1};
    final float[] blue = {0, 0, 1, 1};
    int movementBlue;


    final Risk risk;
    Map<Player, float[]> playerColors = new HashMap<>();
    OverlayController overlayController;

    public View(final Risk risk) {
        movementBlue = Color.parseColor("#ff0099cc");
        this.risk = risk;
        overlayController = MainActivity.overlayController;
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
                    overlayController.addViewChange(R.layout.activity_placestartingarmies);
                    overlayController.replaceText(R.id.armiesToPlace, "Armies to place: " + risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.setBackgroundColour(R.id.placeStartingArmies, Util.getIntFromColor(playerColors.get(risk.getCurrentPlayer())));
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                    System.out.println("Fight Phase view");
                    overlayController.addViewChange(R.layout.activity_placearmies);
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.replaceText(R.id.troopsSelected, "0");
                    overlayController.replaceText(R.id.troopsLeft, "" + overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.FIGHT) {

                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {

                }
            }

            @Override
            public void placeEvent(OverlayChangeEvent overlayChangeEvent) {
                if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES) {
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.replaceText(R.id.troopsLeft, "" + overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    if (overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace() == 0) {
                        overlayController.addViewChange(R.layout.activity_attackphase);
                    }
                } else if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.MOVEMENT && overlayChangeEvent.risk.getSecondSelectedTerritory() != null) {
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getSelectedTerritory().getArmyCount() - 1);
                    overlayController.replaceText(R.id.troopsLeft, "" + (overlayChangeEvent.risk.getSelectedTerritory().getArmyCount() - 1));
                } else {
                    overlayController.setBarMaxValue(R.id.seekBar, 0);
                    overlayController.replaceText(R.id.troopsLeft, "0");
                }
            }

            @Override
            public void playerChangeEvent(OverlayChangeEvent overlayChangeEvent) {
                overlayController.replaceText(R.id.playerTurn, "Player: " + overlayChangeEvent.risk.getCurrentPlayer().getName());
                if (playerColors.get(overlayChangeEvent.risk.getCurrentPlayer()) != null) {
                    overlayController.setBackgroundColour(R.id.playerTurn, Util.getIntFromColor(playerColors.get(overlayChangeEvent.risk.getCurrentPlayer())));
                }
                if (overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_STARTING_ARMIES) {
                    overlayController.addViewChange(R.layout.activity_placestartingarmies);
                    overlayController.replaceText(R.id.armiesToPlace, "Armies to place: " + risk.getCurrentPlayer().getArmiesToPlace());
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
                            overlayController.addViewChange(R.layout.activity_attackphase);
                            overlayController.replaceText(R.id.nextTurnButton, "Next Phase");
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
                            overlayController.addViewChange(R.layout.activity_fightbutton);
                        }
                        if (event.newTerritory == null) {
                            overlayController.addViewChange(R.layout.activity_attackphase);
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
                                overlayController.addViewChange(R.layout.activity_placearmies);
                                overlayController.setBarMaxValue(R.id.seekBar, risk.getCurrentPlayer().getArmiesToPlace());
                                overlayController.replaceText(R.id.troopsSelected, "0");
                                overlayController.replaceText(R.id.troopsLeft, "" + risk.getCurrentPlayer().getArmiesToPlace());
                            }
                        }
                        if (event.newTerritory == null) {
                            if (risk.getGamePhase() == Risk.GamePhase.MOVEMENT) {
                                overlayController.addViewChange(R.layout.activity_movmentphase);
                            } else {
                                overlayController.addViewChange(R.layout.activity_placearmies);
                                overlayController.setBarMaxValue(R.id.seekBar, risk.getCurrentPlayer().getArmiesToPlace());
                                overlayController.replaceText(R.id.troopsSelected, "0");
                                overlayController.replaceText(R.id.troopsLeft, "" + risk.getCurrentPlayer().getArmiesToPlace());
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
                            overlayController.addViewChange(R.layout.activity_placearmies);
                            overlayController.setBarMaxValue(R.id.seekBar, risk.getSelectedTerritory().getArmyCount() - 1);
                            overlayController.replaceText(R.id.troopsLeft, "" + (risk.getSelectedTerritory().getArmyCount() - 1));
                        }
                        if (event.newTerritory == null) {
                            overlayController.addViewChange(R.layout.activity_movmentphase);
                        }
                        break;
                }
            }

        } else if (obs instanceof Territory) {
            if (arg instanceof Territory.ArmyChangeEvent) {
                Territory.ArmyChangeEvent event = (Territory.ArmyChangeEvent) arg;
                GraphicsManager.setArmies(event.territory.getId(), event.newValue);

            } else if (arg instanceof Territory.OccupierChangeEvent) {
                System.out.println("colors?");
                Territory.OccupierChangeEvent event = (Territory.OccupierChangeEvent) arg;
                if (!playerColors.containsKey(event.newValue)) {
                    Player[] players = risk.getPlayers();
                    Random random = new Random(players[playerColors.size()].getParticipantId() +
                            (players[0].getParticipantId() == players[1].getParticipantId() ?
                                    new Random().nextInt(1000) :         //multiplayer - have same color across all units
                                    playerColors.size()));             //singleplayer - have different color every time
                    float[] rndColor = {random.nextFloat(), random.nextFloat(), random.nextFloat(), 1};
                    playerColors.put(event.newValue, rndColor);
                }
                if (risk.getAttackingTerritory() != null) {
                    GraphicsManager.setColor(event.territory.getId(), playerColors.get(event.newValue), risk.getAttackingTerritory().getId());
                } else {
                    GraphicsManager.setColor(event.territory.getId(), playerColors.get(event.newValue));
                }
            }
        }
    }
}
