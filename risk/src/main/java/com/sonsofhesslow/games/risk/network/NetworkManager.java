package com.sonsofhesslow.games.risk.network;

import com.google.android.gms.games.multiplayer.realtime.RealTimeMessage;
import com.google.example.games.basegameutils.BaseGameUtils;
import com.sonsofhesslow.games.risk.Controller;
import com.sonsofhesslow.games.risk.MainActivity;
import com.sonsofhesslow.games.risk.Player;
import com.sonsofhesslow.games.risk.PlayerChangeEvent;
import com.sonsofhesslow.games.risk.PlayerChangeEventListener;
import com.sonsofhesslow.games.risk.Risk;
import com.sonsofhesslow.games.risk.Territory;

import java.io.IOException;

import com.sonsofhesslow.games.risk.graphics.GraphicsManager;

public class NetworkManager implements PlayerChangeEventListener {
    MainActivity activity;
    boolean selfModified;
    public NetworkManager(Risk risk, final MainActivity activity) {
        this.activity = activity;
        for (final Territory territory : risk.getTerritories()) {
            territory.addArmyListeners(new Territory.ArmyChangeListener() {
                @Override
                public void handle(Territory.ArmyChangeEvent armyChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.territoryChangedMessageBuilder(territory, armyChangeEvent.newValue);
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
            territory.addOccupierListeners(new Territory.OccupierChangeListener() {
                @Override
                public void handle(Territory.OccupierChangeEvent occupierChangeEvent) {
                    if (!selfModified) {
                        NetworkMessage message = NetworkMessage.ownerChangedMessageBuilder(territory, occupierChangeEvent.newValue);
                        try {
                            activity.broadcast(message.serialize(), true);
                        } catch (Exception ex) {
                            //
                        }
                    }
                }
            });
        }
        activity.getController().getRiskModel().addPlayerChangeListener(this);
    }


    public void onRealTimeMessageReceived(RealTimeMessage rtm, MainActivity mainActivity){
        byte[] messageBuffer = rtm.getMessageData();
        String sender = rtm.getSenderParticipantId();

        try
        {
            NetworkMessage recievedNetworkData = NetworkMessage.deSerialize(messageBuffer);

            selfModified = true;

            activity.getController().refreshGamePhase();

            switch (recievedNetworkData.action) {
                case armyAmountChange: {
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
                case occupierChange: {
                    System.out.println("rtmr in owner changed");
                    Territory changedTerritory = Controller.getTerritoryById(recievedNetworkData.regionId);
                    Player newOccupier = null;

                    for(Player p : Controller.getRiskModel().getPlayers()) {
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
                    activity.getController().nextPlayer();
                    /*int playerIndex = 0;
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
                    }*/
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

    @Override
    public void changeEvent(PlayerChangeEvent playerChangeEvent) {
        if(!selfModified){
            NetworkMessage message = NetworkMessage.turnChangedMessageBuilder(playerChangeEvent.newPlayer);

            try {
                activity.broadcast(message.serialize(), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

