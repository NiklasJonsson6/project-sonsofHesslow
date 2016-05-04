package com.example.niklas.projectsonsofhesslow;

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


    Risk risk;
    Map<Player,float[]> playerColors = new HashMap<>();

    public View(Risk risk)
    {
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
                    GraphicsManager.setColor(ownerChangeEvent.territory.getId(),playerColors.get(ownerChangeEvent.newValue));
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
                    GraphicsManager.setOutlineColor(riskChangeEvent.newTerritory.getId(), red);
                    GraphicsManager.setHeight(riskChangeEvent.newTerritory.getId(), 0.04f);
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
                    GraphicsManager.setOutlineColor(riskChangeEvent.newTerritory.getId(), green);
                    GraphicsManager.setHeight(riskChangeEvent.newTerritory.getId(), 0.04f);
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
            }
        });
    }
}
