package com.sonsofhesslow.games.risk.network;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.sonsofhesslow.games.risk.Controller;
import com.sonsofhesslow.games.risk.MainActivity;
import com.sonsofhesslow.games.risk.model.Player;
import com.sonsofhesslow.games.risk.model.Risk;
import com.sonsofhesslow.games.risk.model.Territory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class RiskNetworkManager implements Observer {
    boolean selfModified;
    RiskNetwork riskNetwork = null;
    Controller controller;
    GoogleApiClient googleApiClient;
    GooglePlayNetwork googlePlayNetwork;
    Risk riskModel = null;
    MainActivity activity = null;

    public RiskNetworkManager(MainActivity activity) {
        this.activity = activity;
        this.googlePlayNetwork = new GooglePlayNetwork();

        // Create the Google Api Client with access to Games
        this.googleApiClient = new GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(googlePlayNetwork)
                .addOnConnectionFailedListener(googlePlayNetwork)
                .addApi(Games.API).addScope(Games.SCOPE_GAMES)
                .build();

        activity.setmGoogleApiClient(googleApiClient);

        riskNetwork = new RiskNetwork(activity, this.googleApiClient, googlePlayNetwork);

        riskNetwork.setGooglePlayNetwork(googlePlayNetwork);
        googlePlayNetwork.setNetworkTarget(riskNetwork);
    }

    public void update(Observable obs, Object arg) {
        System.out.println("in update risknetworkmanager");
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
            System.out.println("player change event@@@@@@@@@@@@@@@@");
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

    public void setController(Controller controller) {
        this.controller = controller;
        this.controller.setSelfId(riskNetwork.getmMyId().hashCode());

        setRiskModel(this.controller.getRiskModel());
    }

    public void startQuickGame() {
        //1-3 opponents
        final int MIN_OPPONENTS = 1, MAX_OPPONENTS = 3;
        Bundle autoMatchCriteria = RoomConfig.createAutoMatchCriteria(MIN_OPPONENTS,
                MAX_OPPONENTS, 0);
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        rtmConfigBuilder.setMessageReceivedListener(googlePlayNetwork);
        rtmConfigBuilder.setRoomStatusUpdateListener(googlePlayNetwork);
        rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
    }

    public void startInviteGame(Intent data) {
        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // get the automatch criteria
        Bundle autoMatchCriteria = null;
        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);
        if (minAutoMatchPlayers > 0 || maxAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(
                    minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        }

        // create the room
        RoomConfig.Builder rtmConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        rtmConfigBuilder.addPlayersToInvite(invitees);
        rtmConfigBuilder.setMessageReceivedListener(googlePlayNetwork);
        rtmConfigBuilder.setRoomStatusUpdateListener(googlePlayNetwork);
        if (autoMatchCriteria != null) {
            rtmConfigBuilder.setAutoMatchCriteria(autoMatchCriteria);
        }

        Games.RealTimeMultiplayer.create(googleApiClient, rtmConfigBuilder.build());
    }

    public void acceptInviteToRoom(String invId) {
        RoomConfig.Builder roomConfigBuilder = RoomConfig.builder(googlePlayNetwork);
        roomConfigBuilder.setInvitationIdToAccept(invId)
                .setMessageReceivedListener(googlePlayNetwork)
                .setRoomStatusUpdateListener(googlePlayNetwork);
        Games.RealTimeMultiplayer.join(googleApiClient, roomConfigBuilder.build());
    }

    public RiskNetwork getRiskNetwork() {
        return riskNetwork;
    }

    private void setRiskModel(Risk riskModel) {
        System.out.println("setting risk model");
        this.riskModel = riskModel;

        //add to observables
        riskModel.addObserver(this);
        for(Territory territory: riskModel.getTerritories()) {
            territory.addObserver(this);
        }
    }

    public GooglePlayNetwork getGooglePlayNetwork() {
        return googlePlayNetwork;
    }
}

