package com.sonsofhesslow.games.risk;

import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.example.games.basegameutils.BaseGameUtils;

public class NetworkManager {
    MainActivity activity;
    boolean selfModified;
    public NetworkManager(Risk risk, final MainActivity activity) {
        this.activity = activity;
        for (final Territory territory : risk.getTerritories()) {
            territory.addTroupListeners(new Territory.TroupChangeListener() {
                @Override
                public void handle(Territory.TroupChangeEvent troupChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.territoryChangedMessageBuilder(territory);
                        activity.broadcast(message.serialize(), true);
                    }
                }
            });
            territory.addOwnerListeners(new Territory.OwnerChangeListener() {
                @Override
                public void handle(Territory.OwnerChangeEvent ownerChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.ownerChangedMessageBuilder(territory);
                        activity.broadcast(message.serialize(), true);
                    }
                }
            });
        }
    }


    public void onRealTimeMessageReceived(RealTimeMessage rtm, MainActivity mainActivity){
        byte[] messageBuffer = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();

        NetworkMessage recievedNetworkData = NetworkMessage.deseirialize(messageBuffer);

        selfModified = true;

        switch (recievedNetworkData.getAction()) {
            case regionTroupsChange: {
                Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.getRegionID());
                changedTerritory.setArmyCount(recievedNetworkData.getValue());
            }
            break;
            case ownerChange: {
                Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.getRegionID());
                Player newOccupier = null;

                for(Player p : mainActivity.controller.riskModel.getPlayers()) {
                    if(p.getParticipantId() == recievedNetworkData.getOccupierId()){
                        newOccupier = p;
                        break;
                    }
                }

                changedTerritory.setOccupier(newOccupier);
            }
            break;
            default:
                BaseGameUtils.makeSimpleDialog(mainActivity, "Unknown network failure");
        }

        selfModified = false;
    }
}

