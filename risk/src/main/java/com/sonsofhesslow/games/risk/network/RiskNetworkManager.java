package com.sonsofhesslow.games.risk.network;

import com.sonsofhesslow.games.risk.Controller;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

public class RiskNetworkManager implements Observer {
    boolean selfModified;
    RiskNetwork riskNetwork = null;
    Controller controller;

    public RiskNetworkManager(Risk risk, final Controller controller, RiskNetwork riskNetwork, GooglePlayNetwork googlePlayNetwork) {
        this.controller = controller;
        this.riskNetwork = riskNetwork;

        this.controller.setSelfId(riskNetwork.getmMyId().hashCode());

        riskNetwork.setGooglePlayNetwork(googlePlayNetwork);
        googlePlayNetwork.setNetworkTarget(riskNetwork);

        //add to observables
        risk.addObserver(this);
        for(Territory territory: risk.getTerritories()) {
            territory.addObserver(this);
        }
    }

    public void update(Observable obs, Object arg) {
        if (obs instanceof Territory) {
            Territory territory = (Territory) obs;
            if (arg instanceof Integer) {
                /*
                ARMY CHANGE EVENT
                 */
                int event = (Integer) arg;
                if (!selfModified) {
                    RiskNetworkMessage message = RiskNetworkMessage.territoryChangedMessageBuilder(territory, event);
                    try {
                        riskNetwork.broadcast(message.serialize());
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            } else if (arg instanceof Player) {
                /*
                OCCUPIER CHANGE EVENT
                 */
                Player event = (Player) arg;
                if (!selfModified) {
                    RiskNetworkMessage message = RiskNetworkMessage.occupierChangedMessageBuilder(territory, event);
                    try {
                        riskNetwork.broadcast(message.serialize());
                    } catch (Exception ex) {
                        //
                    }
                }
            }
        } else if (obs instanceof Risk) {
            /*
            PLAYER CHANGE EVENT LISTENER
             */
            if (arg instanceof Player) {
                Player event = (Player) arg;
                if(!selfModified){
                    RiskNetworkMessage message = RiskNetworkMessage.turnChangedMessageBuilder(event);

                    try {
                        System.out.println("risknetwork: " + riskNetwork);
                        riskNetwork.broadcast(message.serialize());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public GooglePlayNetworkCompatible getRiskNetwork() {
        return riskNetwork;
    }
}

