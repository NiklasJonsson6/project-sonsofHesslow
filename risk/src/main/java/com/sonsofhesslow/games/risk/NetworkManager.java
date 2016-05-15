package com.sonsofhesslow.games.risk;

import com.google.android.gms.drive.internal.ControlProgressRequest;
import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.example.games.basegameutils.BaseGameUtils;

import Graphics.GraphicsManager;

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
                        NetworkMessage message = NetworkMessage.territoryChangedMessageBuilder(territory, troupChangeEvent.newValue);
                        try
                        {
                            activity.broadcast(message.serialize(), true);
                        }
                        catch (Exception ex)
                        {
                            ex.printStackTrace();
                        }
                    }
                }
            });
            territory.addOwnerListeners(new Territory.OwnerChangeListener() {
                @Override
                public void handle(Territory.OwnerChangeEvent ownerChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.ownerChangedMessageBuilder(territory, ownerChangeEvent.newValue);
                        try {
                            activity.broadcast(message.serialize(), true);
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
            });
        }
    }


    public void onRealTimeMessageReceived(RealTimeMessage rtm, MainActivity mainActivity){
        byte[] messageBuffer = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();

        try
        {
            NetworkMessage recievedNetworkData = NetworkMessage.deSerialize(messageBuffer);

            selfModified = true;

            switch (recievedNetworkData.action) {
                case troupChange: {
                    System.out.println("rtmr region changed");
                    Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.regionId);
                    if(changedTerritory!=null)
                        changedTerritory.setArmyCount(recievedNetworkData.troups);
                    else
                    {
                        System.out.println("illegal region index");
                    }
                }
                break;
                case ownerChange: {
                    System.out.println("rtmr in owner changed");
                    Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.regionId);
                    Player newOccupier = null;

                    for(Player p : Controller.riskModel.getPlayers()) {
                        if(p.getParticipantId() == recievedNetworkData.participantId){
                            System.out.println("found owner");
                            newOccupier = p;
                            break;
                        }
                    }
                    if(changedTerritory!=null)
                    {
                        changedTerritory.setOccupier(newOccupier);
                    }
                    else {
                        System.out.println("illegal region index");
                    }
                }
                break;
                case turnChange: {
                    System.out.println("rtmr turnchange");
                    int playerIndex = 0;
                    int amountOfPlayers = Controller.riskModel.getPlayers().length;
                    for(int i = 0; i < amountOfPlayers; i++) {
                        if(recievedNetworkData.participantId == Controller.riskModel.getPlayers()[i].getParticipantId()){
                            playerIndex = i;
                        }
                    }
                    if(playerIndex == amountOfPlayers - 1){
                        System.out.printf("new players turn (wrap turn)");
                        Controller.riskModel.setCurrentPlayer(Controller.riskModel.getPlayers()[0]);
                    } else {
                        System.out.println("new players turn");
                        Controller.riskModel.setCurrentPlayer(Controller.riskModel.getPlayers()[playerIndex + 1]);
                    }
                }
                break;
                default:{
                    System.out.println("network failure");
                    BaseGameUtils.makeSimpleDialog(mainActivity, "Unknown network failure.\n(Please send an email to onetapchap@gmail.com and tell us how this happend, thank you!)");
                }
            }
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
        }

        selfModified = false;

        GraphicsManager.requestRender();
    }
}

