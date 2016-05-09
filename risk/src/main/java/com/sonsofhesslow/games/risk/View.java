package com.sonsofhesslow.games.risk;

import java.util.HashMap;
import java.util.Map;

import Graphics.GraphicsManager;

/**
 * Created by Daniel on 27/04/2016.
 */
public class View {

    final float[] red = {1,0,0,1};
    final float[] black = {0,0,0,1};
    final float[] green = {0,1,0,1};
    final float[] blue = {0,0,1,1};


    final Risk risk;
    Map<Player,float[]> playerColors = new HashMap<>();
    OverlayController overlayController;

    public View(final Risk risk)
    {
        this.risk = risk;
        overlayController = MainActivity.overlayController;
        for(Territory t : risk.getTerritories())
        {
            t.addOwnerListeners(new Territory.OwnerChangeListener() {
                @Override
                public void handle(Territory.OwnerChangeEvent ownerChangeEvent) {
                    if(!playerColors.containsKey(ownerChangeEvent.newValue))
                    {
                        float[] rndColor = {(float)Math.random(),(float)Math.random(),(float)Math.random(),1};
                        playerColors.put(ownerChangeEvent.newValue,rndColor);
                    }
                    if(risk.getAttackingTerritory()!=null)
                    {
                        GraphicsManager.setColor(ownerChangeEvent.territory.getId(),playerColors.get(ownerChangeEvent.newValue),risk.getAttackingTerritory().getId());
                    }
                    else
                    {
                        GraphicsManager.setColor(ownerChangeEvent.territory.getId(),playerColors.get(ownerChangeEvent.newValue));
                    }
                }
            });

            t.addTroupListeners(new Territory.TroupChangeListener() {
                @Override
                public void handle(Territory.TroupChangeEvent troupChangeEvent) {
                    GraphicsManager.setTroops(troupChangeEvent.territory.getId(), troupChangeEvent.newValue);
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
                if(riskChangeEvent.newTerritory == null){
                    overlayController.addViewChange(R.layout.activity_nextturn);
                    overlayController.replaceText(R.id.nextTurnButton,"Next Phase");
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
                if(riskChangeEvent.newTerritory == null){
                    overlayController.addViewChange(R.layout.activity_nextturn);
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
                }
                if(riskChangeEvent.newTerritory == null){
                    overlayController.addViewChange(R.layout.activity_nextturn);
                    overlayController.replaceText(R.id.nextTurnButton,"Next Turn");
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
                }
                if(riskChangeEvent.newTerritory == null){
                    overlayController.addViewChange(R.layout.activity_nextturn);
                }
            }
        });
        risk.addOverlayListener(new OverlayChangeListener(){
            @Override
            public void phaseEvent(OverlayChangeEvent overlayChangeEvent){
                System.out.println("Niggah");
                System.out.println(overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                if(overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PICK_TERRITORIES){
                    overlayController.addViewChange(R.layout.activity_placearmies);
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.replaceText(R.id.troopsSelected,"0");
                    overlayController.replaceText(R.id.troopsLeft,""+ overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                } else if(overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_STARTING_ARMIES || overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES){
                    overlayController.addViewChange(R.layout.activity_placearmies);
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.replaceText(R.id.troopsSelected,"0");
                    overlayController.replaceText(R.id.troopsLeft,""+ overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                }  if(overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.FIGHT){

                } else if(overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.MOVEMENT){

                }
            }
            @Override
            public void placeEvent(OverlayChangeEvent overlayChangeEvent){
                if(overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_ARMIES || overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.PLACE_STARTING_ARMIES){
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.replaceText(R.id.troopsLeft, "" + overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                } else if(overlayChangeEvent.risk.getGamePhase() == Risk.GamePhase.MOVEMENT && overlayChangeEvent.risk.getSecondSelectedTerritory() != null){
                    overlayController.setBarMaxValue(R.id.seekBar, overlayChangeEvent.risk.getCurrentPlayer().getArmiesToPlace());
                    overlayController.replaceText(R.id.troopsLeft, "" + overlayChangeEvent.risk.getSelectedTerritory());
                } else {
                    overlayController.setBarMaxValue(R.id.seekBar, 0);
                    overlayController.replaceText(R.id.troopsLeft, "0");
                }
            }
            @Override
            public void playerChangeEvent(OverlayChangeEvent overlayChangeEvent){
                overlayController.replaceText(R.id.playerTurn,"Player: " + overlayChangeEvent.risk.getCurrentPlayer().getName());
                if(playerColors.get(overlayChangeEvent.risk.getCurrentPlayer()) != null) {
                    overlayController.setBackgroundColour(R.id.playerTurn, Util.getIntFromColor(playerColors.get(overlayChangeEvent.risk.getCurrentPlayer())));
                }
            }
        });
    }
    public float[] getColor (Player p){
        return playerColors.get(p);
    }
}
